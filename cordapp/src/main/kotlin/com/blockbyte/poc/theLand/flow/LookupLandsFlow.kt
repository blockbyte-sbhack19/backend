package com.blockbyte.poc.theLand.flow

import co.paralleluniverse.fibers.Suspendable
import com.blockbyte.poc.theLand.data.Filter
import com.blockbyte.poc.theLand.data.Land
import com.blockbyte.poc.theLand.data.LandInfo
import com.blockbyte.poc.theLand.data.state.LandSchemaV1
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.whoIs
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.unwrap

class LookupLandsFlow {

    @CordaSerializable
    data class LandCollection(val lands: List<LandInfo>)

    @InitiatingFlow
    @StartableByRPC
    class Search(private val filter: Filter, private val serviceProviderName: CordaX500Name) : FlowLogic<LandCollection>() {

        @Suspendable
        override fun call(): LandCollection {
            return try {
                // TODO: Actually the Oracle has to be query instead of Service Provider
                val flowSession: FlowSession = initiateFlow(whoIs(serviceProviderName))
                flowSession.sendAndReceive<LandCollection>(filter).unwrap { it }
            } catch(e: Exception) {
                logger.error("Can't get available lands.", e)
                throw e
            }
        }
    }

    @InitiatedBy(Search::class)
    class Explorer(private val flowSession: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            try {
                flowSession.receive<Filter>().unwrap { filter ->

                    val lands = builder {
                        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
                        val maxPriceRange = QueryCriteria.VaultCustomQueryCriteria(LandSchemaV1.PersistentLand::price.greaterThan(filter.minPrice))
                        val minPriceRange = QueryCriteria.VaultCustomQueryCriteria(LandSchemaV1.PersistentLand::price.lessThan(filter.maxPrice))

                        val customCriteria = generalCriteria.and(maxPriceRange).and(minPriceRange)

                        serviceHub.vaultService.queryBy<LandState>(customCriteria)
                    }.states

                    // TODO: implement: 1) additional opptions to filter the lands 2) query based on grouped LandState and LeaseState

                    val collection = LandCollection(lands
                            .map { it -> it.state.data }
                            .map { it ->
                                val land = Land(it.landId, it.owner.name.toString())
                                LandInfo(land, it.price, it.details)
                            })

                    flowSession.send(collection)
                }

            } catch (e: Exception) {
                logger.error("The lands look up has been failed", e)
                throw FlowException(e.message)
            }
        }
    }

}