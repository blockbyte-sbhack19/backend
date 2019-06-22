package com.blockbyte.poc.theLand.data.state

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object LandSchema


object LandSchemaV1 : MappedSchema(
    schemaFamily = LandSchema.javaClass,
    version = 1,
    mappedTypes = listOf(PersistentLand::class.java)
) {
    @Entity
    @Table
    class PersistentLand(
            @Column var landId: String
    ) : PersistentState() {  constructor(): this("") }
}