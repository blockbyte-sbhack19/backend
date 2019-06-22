package com.blockbyte.poc.theLand.flow

import co.paralleluniverse.fibers.Suspendable
import com.blockbyte.poc.theLand.data.Land
import com.blockbyte.poc.theLand.data.Lease
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.data.state.LeaseState
import com.blockbyte.poc.theLand.whoAmI
import com.blockbyte.poc.theLand.whoIsNotary
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.builder
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap

class RequestForLeaseFlow {

    @CordaSerializable
    data class LeaseRequest(val landId: String, val lease: Lease)

    @InitiatingFlow
    @StartableByRPC
    class Proposal(val land: Land, val lease: Lease) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            try {
                val flowSession: FlowSession = initiateFlow(land.owner)
                flowSession.send(LeaseRequest(land.id, lease))

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

    @InitiatedBy(Proposal::class)
    class Accept(private val flowSession: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            try {
                val txBuilder = TransactionBuilder(whoIsNotary())

                val (landId, lease) = flowSession.receive<LeaseRequest>().unwrap {
                    proposal -> Pair(proposal.landId, proposal.lease) }

                val leaser = flowSession.counterparty
                val myNode = whoAmI()

                // Stage 1.
                val landState = serviceHub.vaultService.queryBy<LandState>(
                        LandState.buildQuery(landId)
                ).states.single()

                LandState.leaseLand(txBuilder, landState, myNode, leaser)
                LeaseState.createLease(txBuilder, landId, myNode, leaser, lease)

                // Stage 2.
                txBuilder.verify(serviceHub)

                // Stage 3.
                val signedBySelf = serviceHub.signInitialTransaction(txBuilder)
                val signedByAll = subFlow(CollectSignaturesFlow(signedBySelf, listOf(flowSession)))

                // Stage 4.
                subFlow(FinalityFlow(signedByAll))
            } catch (e: Exception) {
                logger.error("The land can not be leased", e)
                throw FlowException(e.message)
            }
        }
    }
}