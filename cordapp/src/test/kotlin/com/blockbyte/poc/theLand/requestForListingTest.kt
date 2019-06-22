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

        val testLand = LandDetails(
                altitude = 1000,
                latitude = 1000,
                size = 10000,
                beforeDate = 40000,
                afterDate = 50000)

        val testPrice = LeasePrice(
                landPrice = 10000,
                feeForStandard = mapOf(
                        Pair(BioStandard.NO_GMO, 1000),
                        Pair(BioStandard.ANIMALS, 2000)
                ),
                feeForCrop = mapOf(
                        Pair(Crop.ANIMALS, 1000),
                        Pair(Crop.NO_GMO, 2000)))

        val requestForListing = RequestForListingFlow.Offer(testLand, testPrice, maintainer.getParty().name)
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