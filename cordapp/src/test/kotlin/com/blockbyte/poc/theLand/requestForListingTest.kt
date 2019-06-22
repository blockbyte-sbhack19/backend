package com.blockbyte.poc.theLand

import com.blockbyte.poc.theLand.data.*
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.flow.RequestForListingFlow
import net.corda.core.utilities.getOrThrow
import org.junit.Test
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.Vault.StateStatus.ALL
import net.corda.core.node.services.queryBy
import kotlin.test.assertEquals


class RequestForListingTest: MockNet()  {

    @Test
    fun `request for new land can be listed`() {

        val testLand = LandProperty(
                coordinate = "coordinate",
                size = 10000,
                beforeDate = 40000,
                afterDate = 50000)

        val testPrice = LeasePrice(
                landPrice = 10000L,
                feeForStandart = mapOf(
                        Pair(BioStandart.NO_GMO, 1000L),
                        Pair(BioStandart.ANIMALS, 2000L)
                ),
                feeForCrop = mapOf(
                        Pair(Crop.ANIMALS, 1000L),
                        Pair(Crop.NO_GMO, 2000L)))

        val requestForListing = RequestForListingFlow.Offer(testLand, testPrice, maintainer.getParty())
        val requestForListingRes = lender.startFlow(requestForListing).toCompletableFuture()

        net.runNetwork()
        requestForListingRes.getOrThrow()

        lender.transaction {
            val query = QueryCriteria.VaultQueryCriteria(ALL)
            val landStates = lender.services.vaultService.queryBy<LandState>(query).states

            assertEquals(1, landStates.size)
        }
    }
}