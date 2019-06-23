/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.blockbyte.web.controllers

import com.blockbyte.poc.theLand.X500Names
import com.blockbyte.poc.theLand.data.Filter
import com.blockbyte.poc.theLand.data.Land
import com.blockbyte.poc.theLand.data.Lease
import com.blockbyte.poc.theLand.data.state.LeaseState
import com.blockbyte.poc.theLand.flow.CompleteLeaseFlow
import com.blockbyte.poc.theLand.flow.RequestForLeaseFlow
import com.blockbyte.poc.theLand.flow.LookupLandsFlow
import com.blockbyte.poc.theLand.flow.LookupLandsFlow.LandCollection
import com.blockbyte.web.components.RPCComponent
import com.blockbyte.web.data.API
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.time.Duration

@RestController
@RequestMapping("api/leaser")
@CrossOrigin
@Profile("leaser")
class LeaserController(rpc: RPCComponent) {
    private final val proxy = rpc.services
    private final val logger = loggerFor<LeaserController>()

    @GetMapping("whoami")
    fun getWhoAmI(): Any {
        return proxy.nodeInfo().legalIdentities.first().name.organisation
    }

    @GetMapping("soils")
    fun getOccupiedLands(): List<API.Occupation>  {
        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
        val leases = proxy.vaultQueryBy<LeaseState>(generalCriteria)

        return leases.states
                .map { it.state.data }
                .map { API.Occupation(
                        it.landId,
                        it.leaser.name.toString(),
                        it.lease.finalPrice,
                        it.lease.beforeDate,
                        it.lease.afterDate)
                }
    }

    @PostMapping("soil/filter")
    fun getAvailableLands(@RequestBody filter: API.Filter): LandCollection {
        val serviceProvider = X500Names.ServiceProvider

        val filter = Filter(
                filter.maxPrice,
                filter.minPrice,
                filter.typeOfCrop,
                filter.bioStandard,
                filter.beforeDate,
                filter.afterDate)

        return proxy.startFlow(LookupLandsFlow::Search, filter, serviceProvider).returnValue.get()
    }

    @PostMapping("soil/lease")
    fun requestForNewLeasing(@RequestBody lease: API.Lease) {
        return try {
            val land = Land(
                    lease.landId,
                    lease.landOwner)

            val lease = Lease(
                    lease.finalPrice,
                    lease.bioStandard,
                    lease.typeOfCrop,
                    lease.beforeDate,
                    lease.afterDate)

            val future = proxy.startFlow(RequestForLeaseFlow::Proposal, land, lease).returnValue
            future.getOrThrow(Duration.ofSeconds(15))

        } catch (e: Exception) {
            logger.error("The land has not been leased", e)
        }
    }

    @PostMapping("soil/free")
    fun completeTheLease(@RequestBody landId: String) {
        return try {
            val future = proxy.startFlow(CompleteLeaseFlow::Leaser, landId).returnValue
            future.getOrThrow(Duration.ofSeconds(15))
        } catch (e: Exception) {
            logger.error("The land has not been released", e)
        }
    }
}
