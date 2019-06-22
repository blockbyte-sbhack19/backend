package com.blockbyte.poc.theLand.data

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class BioStandart {
    UNKNOWN,
    ORGANIC,
    NO_GMO,
    PESTICIDE,
    ANIMALS
}

@CordaSerializable
enum class Crop {
    UNKNOWN,
    ORGANIC,
    NO_GMO,
    PESTICIDE,
    ANIMALS
}

@CordaSerializable
data class LandProperty(
        val location: String,
        val size: Long,
        val beforeDate: Long,
        val afterDate: Long)

@CordaSerializable
data class LeasePrice(
        val landPrice: Long,
        val feeForStandart: Map<BioStandart, Long>,
        val feeForCrop: Map<Crop, Long>)

@CordaSerializable
data class Land(
        val id: String,
        val owner: Party)

@CordaSerializable
data class Lease(
        val price: Long,
        val standart: BioStandart,
        val typeOfCrop: Crop,
        val beforeDate: Long,
        val afterDate: Long)

