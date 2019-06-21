package com.blockbyte.poc.theLand.flow

import co.paralleluniverse.fibers.Suspendable
import com.blockbyte.poc.theLand.data.LandProperty
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.whoAmI
import com.blockbyte.poc.theLand.whoIsNotary
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.util.*

class RequestForListingFlow {

    @CordaSerializable
    data class LandRecord(val legalLandId: String)

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

        @Suspendable
        override fun call() {
            try {

                val txBuilder = TransactionBuilder(whoIsNotary())

                flowSession.receive<LandProperty>().unwrap { requestForListing ->
                    // Stage 1.
                    val landRecord = queryLandRegistry(requestForListing.location)
                    // TODO: request personal credential and check against proper ownership of the land

                    // Stage 2.
                    val owner = flowSession.counterparty
                    val myNode = whoAmI()

                    LandState.createLand(txBuilder, landRecord.legalLandId, requestForListing, owner, myNode)
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

        // TODO: query to oracle
        @Suspendable
        private fun queryLandRegistry(location: String): LandRecord =
            LandRecord(UUID.randomUUID().toString())
    }
}