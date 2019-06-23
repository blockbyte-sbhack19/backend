package com.blockbyte.poc.theLand.flow

import co.paralleluniverse.fibers.Suspendable
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.data.state.LeaseState
import com.blockbyte.poc.theLand.whoAmI
import com.blockbyte.poc.theLand.whoIsNotary
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy

import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

class CompleteLeaseFlow {

    @InitiatingFlow
    @StartableByRPC
    class Leaser(val landId: String) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            try {
                val txBuilder = TransactionBuilder(whoIsNotary())

                val landState = serviceHub.vaultService.queryBy<LandState>(
                        LandState.buildQuery(landId)
                ).states.single()

                val leaseState = serviceHub.vaultService.queryBy<LeaseState>(
                        LeaseState.buildQuery(landId)
                ).states.single()

                LandState.freeLand(txBuilder, landState, whoAmI())
                LeaseState.closeLease(txBuilder, leaseState)

                val flowSession = initiateFlow(leaseState.state.data.lender)

                val signedBySelf = serviceHub.signInitialTransaction(txBuilder)
                val signedByAll = subFlow(CollectSignaturesFlow(signedBySelf, listOf(flowSession)))

                subFlow(FinalityFlow(signedByAll))

            } catch (e: Exception) {
                logger.error("The land can not be listed", e)
            }
        }
    }

    @InitiatedBy(Leaser::class)
    class Lender(private val flowSession: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val verifyTxFlow = object : SignTransactionFlow(flowSession) {
                @Suspendable
                override fun checkTransaction(stx: SignedTransaction) {
                    // TODO: check nothing has been changed in the final object
                }
            }
            waitForLedgerCommit(subFlow(verifyTxFlow).id)
        }
    }
}