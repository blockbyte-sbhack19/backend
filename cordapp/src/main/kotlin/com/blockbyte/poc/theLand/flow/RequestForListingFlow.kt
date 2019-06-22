package com.blockbyte.poc.theLand.flow

import co.paralleluniverse.fibers.Suspendable
import com.blockbyte.poc.theLand.data.LandDetails
import com.blockbyte.poc.theLand.data.LeasePrice
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.whoAmI
import com.blockbyte.poc.theLand.whoIs
import com.blockbyte.poc.theLand.whoIsNotary
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.util.*

class RequestForListingFlow {

    @CordaSerializable
    data class LandOffer(val details: LandDetails, val price: LeasePrice)

    @CordaSerializable
    data class LandRecord(val landId: String)

    @InitiatingFlow
    @StartableByRPC
    class Offer(private val landDetails: LandDetails,
                private val leasePrice: LeasePrice,
                private val serviceProviderName: CordaX500Name) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            try {
                val flowSession: FlowSession = initiateFlow(whoIs(serviceProviderName))

                flowSession.send(LandOffer(landDetails, leasePrice))

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

                val (land, price) = flowSession.receive<LandOffer>().unwrap {
                    offer -> Pair(offer.details, offer.price) }

                // Stage 1.
                val landRecord = queryLandRegistry(land.altitude, land.latitude)
                // TODO: request personal credential and check against proper ownership of the land

                // Stage 2.
                val owner = flowSession.counterparty
                val myNode = whoAmI()

                LandState.createLand(txBuilder, landRecord.landId, land, price, owner, myNode)

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
        private fun queryLandRegistry(altitude: Long, latitude: Long): LandRecord =
            LandRecord(UUID.randomUUID().toString())
    }
}