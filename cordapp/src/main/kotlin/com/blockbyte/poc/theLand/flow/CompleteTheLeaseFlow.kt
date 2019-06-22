package com.blockbyte.poc.theLand.flow

import co.paralleluniverse.fibers.Suspendable
import com.blockbyte.poc.theLand.data.Land
import net.corda.core.flows.*

import net.corda.core.transactions.SignedTransaction

class CompleteTheLeaseFlow {

    @InitiatingFlow
    @StartableByRPC
    class Leaser(val landId: Land) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            try {

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