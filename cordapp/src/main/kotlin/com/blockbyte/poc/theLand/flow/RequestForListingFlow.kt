package com.blockbyte.poc.theLand.flow

import co.paralleluniverse.fibers.Suspendable
import com.blockbyte.poc.theLand.data.LandProperty
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.regoracle.flow.LandRegistryOracleQuery
import com.blockbyte.poc.theLand.regoracle.flow.LandRegistryOracleQueryHandler.LandRecord
import com.blockbyte.poc.theLand.regoracle.flow.LandRegistryOracleQuery.LandRecordRequest
import com.blockbyte.poc.theLand.whoIs
import com.blockbyte.poc.theLand.whoIsNotary
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap

class RequestForListingFlow {

    @InitiatingFlow
    @StartableByRPC
    class Offer(val landProperty: LandProperty, val maintainer: Party) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            try {
                val flowSession: FlowSession = initiateFlow(maintainer)

                flowSession.send(landProperty)

                val verifyTxFlow = object : SignTransactionFlow(flowSession) {
                    @Suspendable
                    override fun checkTransaction(stx: SignedTransaction) {
                        // TODO: check nothing has been changed in the final object
                    }
                }

                waitForLedgerCommit(subFlow(verifyTxFlow).id)

            } catch (e: Exception) {
                logger.error("The land can not be listed, the verifier interrupt the procedure", e)
            }
        }
    }

    @InitiatedBy(Offer::class)
    class ValidateOffer(private val flowSession: FlowSession) : FlowLogic<Unit>() {

        val owner = flowSession.counterparty


        @Suspendable
        override fun call() {
            try {
                val txBuilder = TransactionBuilder(whoIsNotary())

                flowSession.receive<LandProperty>().unwrap { requestForListing ->
                    // Stage 1.
                    val landRecord = queryLandRegistry(requestForListing.location)
                    // TODO: request personal credential and check against proper ownership of the land

                    // Stage 2.
                    LandState.createLand(txBuilder, landRecord.legalLandId, requestForListing, owner)
                }

                // Stage 3.
                txBuilder.verify(serviceHub)

                // Stage 4.
                val signedBySelf = serviceHub.signInitialTransaction(txBuilder)
                val signedByAll = subFlow(CollectSignaturesFlow(signedBySelf, listOf(flowSession)))

                // Stage 5.
                subFlow(FinalityFlow(signedByAll))
            } catch (e: Exception) {
                logger.error("The can not be listed", e)
                throw FlowException(e.message)
            }
        }

        @Suspendable
        private fun queryLandRegistry(location: String): LandRecord {
            val oracle = whoIs(CordaX500Name("LandRegistry", "Swiss", "CH"))
            return subFlow(LandRegistryOracleQuery(oracle, LandRecordRequest(location)))
        }
    }

    @InitiatingFlow
    @SchedulableFlow
    class AutoRelease(val stateRef: StateRef) : FlowLogic<Unit>() {

        override fun call() {
        }
    }
}