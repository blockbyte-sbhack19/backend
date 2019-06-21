package com.blockbyte.poc.theLand

import com.blockbyte.poc.theLand.data.LandProperty
import com.blockbyte.poc.theLand.data.state.LandState
import com.blockbyte.poc.theLand.flow.RequestForListingFlow
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.Vault.StateStatus.ALL
import net.corda.core.node.services.queryBy
import kotlin.test.assertEquals

class RequestForListingTest {

    lateinit var net: MockNetwork

    lateinit var lender: StartedMockNode
    lateinit var maintainer: StartedMockNode

    @Before
    fun up() {
        net = MockNetwork(
                cordappPackages = listOf("com.blockbyte.poc.theLand"),
                threadPerNode = false
        )

        lender = net.createPartyNode(CordaX500Name("Lender", "US", "US"))
        maintainer = net.createPartyNode(CordaX500Name("Maintainer", "US", "US"))

        net.runNetwork()
    }

    @After
    fun down() {
        net.stopNodes()
    }

    @Test
    fun `request for new land can be listed`() {

        val testLand = LandProperty(
                location = "location",
                size = 10000,
                minPrice = 20000,
                beforeDate = 40000,
                afterDate = 50000,
                allowedMachine = emptyList())

        val flowAskForPackage = RequestForListingFlow.Offer(testLand, maintainer.info.singleIdentity())
        val requestForListingRes = lender.startFlow(flowAskForPackage).toCompletableFuture()

        net.runNetwork()
        requestForListingRes.getOrThrow()

        lender.transaction {
            val query = QueryCriteria.VaultQueryCriteria(ALL)
            val landStates = lender.services.vaultService.queryBy<LandState>().states

            assertEquals(1, landStates.size)
        }
    }
}