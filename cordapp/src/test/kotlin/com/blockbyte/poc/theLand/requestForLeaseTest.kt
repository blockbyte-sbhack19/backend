package com.blockbyte.poc.theLand

import com.blockbyte.poc.theLand.contract.LandOperationalContract
import com.blockbyte.poc.theLand.contract.LandOperationalContract.Companion.ID
import com.blockbyte.poc.theLand.data.*
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.flow.RequestForLeaseFlow
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.Vault.StateStatus.ALL
import net.corda.testing.node.ledger
import org.junit.Test
import kotlin.test.assertEquals

class RequestForLeasingTest: MockNet() {

    @Test
    fun `lease the land`() {

        val property = LandDetails("coordinate", 1, 100, 100)
        val price = LeasePrice(1000, mapOf(), mapOf())

        val testLandState = LandState("landid#1", property, price,
                participants = listOf(lender.getParty(), maintainer.getParty()))

        lender.services.ledger {
            transaction {
                command(lender.getKey(), LandOperationalContract.Commands.OfferLand())
                output(ID, "lender test land", testLandState)
                verifies()
            }
        }

        net.runNetwork()

        val testLand = Land(testLandState.landId, lender.getParty())
        val testLease = Lease(
                testLandState.price.landPrice,
                BioStandard.UNKNOWN,
                Crop.UNKNOWN,
                testLandState.details.beforeDate,
                testLandState.details.afterDate)

        val requestForLeasing = RequestForLeaseFlow.Proposal(testLand, testLease)
        val requestForListingRes = lender.startFlow(requestForLeasing).toCompletableFuture()

        net.runNetwork()
        requestForListingRes.getOrThrow()

        lender.transaction {
            val query = QueryCriteria.VaultQueryCriteria(ALL)
            val landStates = lender.services.vaultService.queryBy<LandState>(query).states

            assertEquals(2, landStates.size)
        }
    }
}