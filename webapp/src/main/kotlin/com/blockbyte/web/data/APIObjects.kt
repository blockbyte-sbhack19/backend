package com.blockbyte.web.data

import com.blockbyte.poc.theLand.data.BioStandart
import com.blockbyte.poc.theLand.data.Crop

val SUCCESS = mapOf("success" to true)
val FAILURE = mapOf("success" to false)

interface API {

    data class Land(
            val coordinate: String,
            val landSize: Long,
            val landPrice: Long,
            val feeForStandart: Map<BioStandart, Long>,
            val feeForCrop: Map<Crop, Long>,
            val beforeDate: Long,
            val afterDate: Long)

    data class Lease(
            val landId: String,
            val landOwner: String,
            val bioStandart: BioStandart,
            val typeOfCrop: Crop,
            val finalPrice: Long,
            val beforeDate: Long,
            val afterDate: Long)
}