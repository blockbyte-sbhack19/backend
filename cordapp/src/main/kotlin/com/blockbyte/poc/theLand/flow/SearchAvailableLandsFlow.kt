package com.blockbyte.poc.theLand.flow

import co.paralleluniverse.fibers.Suspendable
import com.blockbyte.poc.theLand.data.Filter
import com.blockbyte.poc.theLand.data.Land
import com.blockbyte.poc.theLand.data.LandInfo
import com.blockbyte.poc.theLand.data.LeasePrice
import com.blockbyte.poc.theLand.data.state.LandSchema
import com.blockbyte.poc.theLand.data.state.LandSchemaV1
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.whoIs
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.unwrap

class SearchAvailableLandsFlow {

    @CordaSerializable
    data class LandCollection(val lands: List<LandInfo>)

    @InitiatingFlow
    @StartableByRPC
    class Search(private val filter: Filter, private val serviceProviderName: CordaX500Name) : FlowLogic<LandCollection>() {

        @Suspendable
        override fun call(): LandCollection {
            // TODO: Actually the Oracle has to be query instead of Service Provider
            val flowSession: FlowSession = initiateFlow(whoIs(serviceProviderName))
            return flowSession.sendAndReceive<LandCollection>(filter).unwrap { it }
        }
    }

    @InitiatedBy(Search::class)
    class A(private val flowSession: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            try {
                flowSession.receive<Filter>().unwrap { filter ->

                    val lands = builder {
                        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
                        val maxPriceRange = QueryCriteria.VaultCustomQueryCriteria(LandSchemaV1.PersistentLand::size.greaterThan(filter.minPrice))
                        val minPriceRange = QueryCriteria.VaultCustomQueryCriteria(LandSchemaV1.PersistentLand::size.lessThan(filter.maxPrice))

                        val customCriteria = generalCriteria.and(maxPriceRange).and(minPriceRange)

                        serviceHub.vaultService.queryBy<LandState>(customCriteria)
                    }.states


                    flowSession.send((lands
                            .map { it -> it.state.data }
                            .map { it ->
                                val land = Land(it.landId, "")
                                LandInfo(land, it.price, it.details)
                            }))
                }

            } catch (e: Exception) {
                logger.error("The can not be listed", e)
                throw FlowException(e.message)
            }
        }
    }

}