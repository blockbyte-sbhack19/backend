package com.blockbyte.poc.theLand.data

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class LandProperty(
        val location: String,
        val size: Long,
        val minPrice: Long,
        val beforeDate: Long,
        val afterDate: Long,
        val isAnimalAllowed: Boolean = false,
        val isPesticideAllowed: Boolean = false,
        val allowedMachine: List<String>)

