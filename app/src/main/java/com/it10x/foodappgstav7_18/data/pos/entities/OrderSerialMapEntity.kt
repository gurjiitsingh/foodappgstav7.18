


package com.it10x.foodappgstav7_18.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_serial_map")
data class OrderSerialMapEntity(

    @PrimaryKey
    val mapKey: String,

    val orderSerialNo: Long,

    val createdAt: Long = System.currentTimeMillis()
)
