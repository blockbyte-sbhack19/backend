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

import com.blockbyte.poc.theLand.data.Land
import com.blockbyte.poc.theLand.data.Lease
import com.blockbyte.poc.theLand.flow.RequestForLeaseFlow
import com.blockbyte.web.components.RPCComponent
import com.blockbyte.web.data.API
import net.corda.core.identity.CordaX500Name
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

    @GetMapping("soil")
    fun getAvailableLands() {}

    @PostMapping("soil")
    fun resultForNewLeasing(@RequestBody lease: API.Lease) {
        return try {
            val owner = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(lease.landOwner))

            val land = Land(
                    lease.landId,
                    owner!!)

            val lease = Lease(
                    lease.finalPrice,
                    lease.bioStandart,
                    lease.typeOfCrop,
                    lease.beforeDate,
                    lease.afterDate)

            val future = proxy.startFlowDynamic(
                    RequestForLeaseFlow.Proposal::class.java, land, lease).returnValue

            future.getOrThrow(Duration.ofSeconds(15))

        } catch (e: Exception) {
            logger.error("The land has not beed leased", e)
        }
    }
}
