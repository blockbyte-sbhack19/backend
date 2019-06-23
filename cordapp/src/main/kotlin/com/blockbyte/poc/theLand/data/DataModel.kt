package com.blockbyte.poc.theLand.data

import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class BioStandard {
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

// TODO: I dont like to have dates here, need to move away
@CordaSerializable
data class LandDetails(
        val latitude: Long,
        val altitude: Long,
        val size: Int,
        val beforeDate: Long,
        val afterDate: Long)

@CordaSerializable
data class LeasePrice(
        val landPrice: Int,
        val feeForStandard: Map<BioStandard, Int>,
        val feeForCrop: Map<Crop, Int>)

@CordaSerializable
data class Land(
        val id: String,
        val owner: String)

@CordaSerializable
data class LandInfo(
        val land: Land,
        val lease: LeasePrice,
        val details: LandDetails
)

@CordaSerializable
data class Lease(
        val finalPrice: Int,
        val bioStandards: BioStandard,
        val typeOfCrop: Crop,
        val beforeDate: Long,
        val afterDate: Long)

@CordaSerializable
data class Filter(
        val maxPrice: Int,
        val minPrice: Int,
        val typeOfCrop:  List<Crop>,
        val bioStandard: List<BioStandard>,
        val beforeDate: Long,
        val afterDate:  Long)

