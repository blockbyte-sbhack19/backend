package com.blockbyte.poc.theLand.data.schema

import com.blockbyte.poc.theLand.data.BioStandart
import com.blockbyte.poc.theLand.data.Crop
import com.blockbyte.poc.theLand.data.Lease
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object LeaseSchema


object LeaseSchemaV1 : MappedSchema(
        schemaFamily = LeaseSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentLease::class.java)
) {
    @Entity
    @Table
    class PersistentLease(
            @Column var landId: String,
            @Column var crop: Crop,
            @Column var standart: BioStandart,
            @Column var price: Long,
            @Column var beforeDate: Long,
            @Column var afterDate: Long
    ) : PersistentState() {
        constructor(landId: String, lease: Lease): this(
                landId,
                lease.typeOfCrop,
                lease.standart,
                lease.price,
                lease.beforeDate,
                lease.afterDate)
    }
}