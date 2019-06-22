package com.blockbyte.poc.theLand.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class LandOperationalContract : Contract {
    companion object {
        @JvmStatic
        val ID = LandOperationalContract::class.java.name
    }

    interface Commands : CommandData {
        class OfferLand : TypeOnlyCommandData(), Commands
        class LeaseLand : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {
    }
}