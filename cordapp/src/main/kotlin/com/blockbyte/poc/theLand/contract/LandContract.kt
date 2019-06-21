package com.blockbyte.poc.theLand.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class LandContract : Contract {
    companion object {
        @JvmStatic
        val ID = LandContract::class.java.name
    }

    interface Commands : CommandData {
        class CreateLand : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}