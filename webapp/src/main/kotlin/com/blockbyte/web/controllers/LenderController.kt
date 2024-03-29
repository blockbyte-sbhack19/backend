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
import com.blockbyte.poc.theLand.data.LandDetails
import com.blockbyte.poc.theLand.data.LeasePrice
import com.blockbyte.poc.theLand.flow.RequestForListingFlow
import com.blockbyte.web.components.RPCComponent
import com.blockbyte.web.data.API
import com.blockbyte.web.data.FAILURE
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.time.Duration


@RestController
@RequestMapping("api/lender")
@CrossOrigin
@Profile("lender")
class LenderController(val rpc: RPCComponent) {
    private final val proxy = rpc.services
    private final val logger = loggerFor<LenderController>()

    @GetMapping("whoami")
    fun getWhoAmI(): Any {
        return proxy.nodeInfo().legalIdentities.first().name.organisation
    }

    @PostMapping("soil")
    fun requestForNewListing(@RequestBody land: API.Land): Any {
        return try {
            val landProperty = LandDetails(land.altitude, land.latitude, land.landSize, land.beforeDate, land.afterDate)
            val leasePrice = LeasePrice(land.landPrice, land.feeForStandard, land.feeForCrop)

            val future = proxy.startFlow(RequestForListingFlow::Offer,
                    landProperty, leasePrice, X500Names.ServiceProvider).returnValue

            future.getOrThrow(Duration.ofSeconds(15))

        } catch (e: Exception) {
            logger.error("The land has not beed listed", e)
            FAILURE.plus("error" to e.message)
        }
    }
}
