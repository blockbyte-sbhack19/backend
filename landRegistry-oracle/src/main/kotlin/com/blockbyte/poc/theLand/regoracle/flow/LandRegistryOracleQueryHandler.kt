package com.blockbyte.poc.theLand.regoracle.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.serialization.CordaSerializable
import java.util.*

@InitiatedBy(LandRegistryOracleQuery::class)
class LandRegistryOracleQueryHandler(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    @CordaSerializable
    data class LandRecord(val legalLandId: String, val legalOwner: String)

    @Suspendable
    override fun call() {
        flowSession.send(LandRecord(UUID.randomUUID().toString(), "TODO"))
    }
}
