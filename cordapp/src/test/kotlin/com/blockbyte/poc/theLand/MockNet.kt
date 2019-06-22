package com.blockbyte.poc.theLand

import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before

import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.makeTestIdentityService
import net.corda.testing.core.TestIdentity

open class MockNet {

    lateinit var net: MockNetwork

    val lenderName = CordaX500Name("Lender", "US", "US")
    val leaserName = CordaX500Name("Leaser", "US", "US")

    lateinit var lender: StartedMockNode
    lateinit var leaser: StartedMockNode
    lateinit var maintainer: StartedMockNode

    @Before
    fun up() {
        net = MockNetwork(
                cordappPackages = listOf("com.blockbyte.poc.theLand"),
                threadPerNode = false
        )

        lender = net.createPartyNode(lenderName)
        leaser = net.createPartyNode(leaserName)
        maintainer = net.createPartyNode(CordaX500Name("Maintainer", "US", "US"))

        net.runNetwork()
    }

    @After
    fun down() {
        net.stopNodes()
    }

    companion object {
        fun StartedMockNode.getParty() = this.info.singleIdentity()
        fun StartedMockNode.getKey() = this.getParty().owningKey
    }
}