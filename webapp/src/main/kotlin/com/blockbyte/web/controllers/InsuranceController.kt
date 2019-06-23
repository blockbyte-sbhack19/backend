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

import com.blockbyte.web.components.RPCComponent
import com.blockbyte.web.data.API
import com.blockbyte.web.data.FAILURE
import net.corda.core.utilities.loggerFor
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/insurance")
@CrossOrigin
@Profile("leaser")
class InsuranceController(val rpc: RPCComponent) {
    private final val proxy = rpc.services
    private final val logger = loggerFor<InsuranceController>()

    @PostMapping("payPremium")
    fun requestForPay(@RequestBody land: API.Land): Any {
        return try {
            // TODO with web3j
            // create TX and pay premium for farmer
        } catch (e: Exception) {
            logger.error("the insurance failed", e)
            FAILURE.plus("error" to e.message)
        }
    }

    @PostMapping("collectInsuredPremium")
    fun requestForInsuredPremium(@RequestBody land: API.Land): Any {
        return try {
            // TODO with web3j
            // create TX and withdraw insured premium for farmer
        } catch (e: Exception) {
            logger.error("failed to collect insured premium for farmer", e)
            FAILURE.plus("error" to e.message)
        }
    }

    @PostMapping("numberOfDayWithoutRain")
    fun requestForNumberOfDayWithoutRain(@RequestBody land: API.Land): Any {
        return try {
            // TODO with web3j
            // read from Dry smart contract

        } catch (e: Exception) {
            logger.error("failed to collect number Of day without rain", e)
            FAILURE.plus("error" to e.message)
        }
    }
    @PostMapping("doesInsuranceExist")
    fun requestForDoesInsuranceExist(@RequestBody land: API.Land): Any {
        return try {
            // TODO with web3j
            // read from Dry smart contract doesInsuranceExist(farmer public key)
            // return true/false
        } catch (e: Exception) {
            logger.error("failed to collect number Of day without rain", e)
            FAILURE.plus("error" to e.message)
        }
    }

}
