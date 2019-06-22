package com.blockbyte.poc.theLand.data.state

import com.blockbyte.poc.theLand.contract.LandOperationalContract
import com.blockbyte.poc.theLand.data.Lease
import com.blockbyte.poc.theLand.data.schema.LeaseSchemaV1
import net.corda.core.contracts.Command
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.TransactionBuilder

data class LeaseState(
        val landId: String,
        val lender: Party,
        val leaser: Party,
        val lease: Lease) : ContractState, QueryableState {
    override val participants: List<AbstractParty> = listOf(lender, leaser)

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(LeaseSchemaV1)
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is LeaseSchemaV1 -> LeaseSchemaV1.PersistentLease(landId, lease)
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    companion object {
        fun createLease(trxBuilder: TransactionBuilder,
                        landId: String,
                        lender: Party,
                        leaser: Party,
                        lease: Lease) {
            val landState = LeaseState(landId, lender, leaser, lease)
            val txCommand = Command(LandOperationalContract.Commands.LeaseLand(), listOf(leaser.owningKey, lender.owningKey))

            trxBuilder.addOutputState(landState, LandOperationalContract.ID).addCommand(txCommand)
        }
    }
}