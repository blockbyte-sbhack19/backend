package com.blockbyte.poc.theLand.regoracle.flow

import net.corda.core.flows.InitiatingFlow
import net.corda.core.identity.Party

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.unwrap
import com.blockbyte.poc.theLand.regoracle.flow.LandRegistryOracleQueryHandler.LandRecord

/** Called by the client to request an oracle to split lists. */
@InitiatingFlow
class LandRegistryOracleQuery(
        private val oracle: Party,
        private val request: LandRecordRequest
) : FlowLogic<LandRecord>() {

    @CordaSerializable
    data class LandRecordRequest(val location: String)

    @Suspendable
    override fun call(): LandRecord {
        val oracleSession = initiateFlow(oracle)
        return oracleSession.sendAndReceive<LandRecord>(request).unwrap { it }
    }
}
