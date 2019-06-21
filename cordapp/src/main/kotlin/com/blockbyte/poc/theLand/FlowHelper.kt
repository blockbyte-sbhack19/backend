package com.blockbyte.poc.theLand

import net.corda.core.flows.FlowLogic
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party

fun FlowLogic<Any>.whoIsNotary(): Party {
    return serviceHub.networkMapCache.notaryIdentities.single()
}

fun FlowLogic<Any>.whoAmI(): Party {
    return serviceHub.myInfo.legalIdentities.single()
}

fun FlowLogic<Any>.whoIs(x500: CordaX500Name): Party {
    return serviceHub.identityService.wellKnownPartyFromX500Name(x500)
            ?: throw IllegalStateException("Can't find a node for $x500")
}