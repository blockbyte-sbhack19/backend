package com.blockbyte.poc.theLand.data.state

import com.blockbyte.poc.theLand.contract.LandOperationalContract
import com.blockbyte.poc.theLand.data.LandDetails
import com.blockbyte.poc.theLand.data.LeasePrice
import net.corda.core.contracts.Command
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
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
        val owner: Party,
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
        fun registerLand(trxBuilder: TransactionBuilder,
                         landId: String,
                         landDetails: LandDetails,
                         leasePrice : LeasePrice,
                         landOwner: Party,
                         provider: Party) {
            val landState = LandState(
                    landId,
                    landDetails,
                    leasePrice,
                    owner = landOwner,
                    participants = listOf(landOwner, provider))
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

        fun freeLand(trxBuilder: TransactionBuilder,
                     land: StateAndRef<LandState>,
                     leaser: AbstractParty) {
            val owner = land.state.data.owner
            val participants = land.state.data.participants - leaser
            val leaseLand = land.state.data.copy(status = Status.FREE, participants = participants)
            val txCommand = Command(LandOperationalContract.Commands.FreeLand(), listOf(leaser.owningKey, owner.owningKey))

            trxBuilder.addInputState(land)
            trxBuilder.addOutputState(leaseLand, LandOperationalContract.ID).addCommand(txCommand)
        }

        fun buildQuery(landId: String) = builder {
            QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
                    .and(QueryCriteria.VaultCustomQueryCriteria(LandSchemaV1.PersistentLand::landId.equal(landId)))
        }
    }
}