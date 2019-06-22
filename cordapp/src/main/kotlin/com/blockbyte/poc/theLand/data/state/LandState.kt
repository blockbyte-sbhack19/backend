package com.blockbyte.poc.theLand.data.state

import com.blockbyte.poc.theLand.contract.LandOperationalContract
import com.blockbyte.poc.theLand.data.LandDetails
import com.blockbyte.poc.theLand.data.LeasePrice
import net.corda.core.contracts.Command
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.TransactionBuilder

@CordaSerializable
enum class Status { FREE, OCCUPIED, UNAVAILABLE }

data class LandState(
        val landId: String,
        val details: LandDetails,
        val price: LeasePrice,
        val status: Status =  Status.FREE,
        override val participants: List<AbstractParty>,
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, QueryableState {
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(LandSchemaV1)
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is LandSchemaV1 -> LandSchemaV1.PersistentLand(
                    landId,
                    price.landPrice,
                    details.size,
                    details.beforeDate,
                    details.afterDate)
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    companion object {
        fun createLand(trxBuilder: TransactionBuilder,
                       landId: String,
                       landDetails: LandDetails,
                       leasePrice : LeasePrice,
                       landOwner: AbstractParty,
                       maintainer: AbstractParty) {
            val landState = LandState(landId, landDetails, leasePrice, participants = listOf(landOwner, maintainer))
            val txCommand = Command(LandOperationalContract.Commands.OfferLand(), landOwner.owningKey)

            trxBuilder.addOutputState(landState, LandOperationalContract.ID).addCommand(txCommand)
        }

        fun leaseLand(trxBuilder: TransactionBuilder,
                      land: StateAndRef<LandState>,
                      lender:  AbstractParty,
                      leaser: AbstractParty) {
            val participants = land.state.data.participants + leaser
            val leaseLand = land.state.data.copy(status = Status.OCCUPIED, participants = participants)
            val txCommand = Command(LandOperationalContract.Commands.LeaseLand(), listOf(leaser.owningKey, lender.owningKey))

            trxBuilder.addInputState(land)
            trxBuilder.addOutputState(leaseLand, LandOperationalContract.ID).addCommand(txCommand)
        }

        fun where(landId: String) =
            QueryCriteria.VaultCustomQueryCriteria(LandSchemaV1.PersistentLand::landId.equal(landId))

        fun buildQuery(landId: String) = builder {
            QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED).and(where(landId))
        }
    }
}