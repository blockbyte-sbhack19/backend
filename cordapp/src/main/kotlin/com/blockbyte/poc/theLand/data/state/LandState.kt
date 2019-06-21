package com.blockbyte.poc.theLand.data.state

import com.blockbyte.poc.theLand.contract.LandContract
import com.blockbyte.poc.theLand.data.LandProperty
import net.corda.core.contracts.Command
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.TransactionBuilder

data class LandState(
        val legalLandId: String,
        val property: LandProperty,
        override val participants: List<AbstractParty>,
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, QueryableState {
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(LandSchemaV1)
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is LandSchemaV1 -> LandSchemaV1.PersistentLand(this.legalLandId)
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    companion object {
        fun createLand(trxBuilder: TransactionBuilder,
                       legalLandId: String,
                       landProperty: LandProperty,
                       landOwner: AbstractParty): UniqueIdentifier {
            val landState = LandState(legalLandId, landProperty, listOf(landOwner))
            val txCommand = Command(LandContract.Commands.CreateLand(), landOwner.owningKey)

            trxBuilder.addOutputState(landState, LandContract.ID).addCommand(txCommand)

            return landState.linearId
        }
    }
}