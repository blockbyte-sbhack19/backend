package com.blockbyte.web.data

import com.blockbyte.poc.theLand.data.BioStandard
import com.blockbyte.poc.theLand.data.Crop

val SUCCESS = mapOf("success" to true)
val FAILURE = mapOf("success" to false)

interface API {

    data class Land(
            val latitude: Long,
            val altitude: Long,
            val landSize: Int,
            val landPrice: Int,
            val feeForStandard: Map<BioStandard, Int>,
            val feeForCrop: Map<Crop, Int>,
            val beforeDate: Long,
            val afterDate: Long)

    data class Lease(
            val landId: String,
            val landOwner: String,
            val bioStandard: BioStandard,
            val typeOfCrop: Crop,
            val finalPrice: Int,
            val beforeDate: Long,
            val afterDate: Long)

    data class Filter(
            val maxPrice: Int,
            val minPrice: Int,
            val typeOfCrop:  List<Crop>,
            val bioStandard: List<BioStandard>,
            val beforeDate: Long,
            val afterDate:  Long)
}