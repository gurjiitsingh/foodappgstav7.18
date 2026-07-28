package com.it10x.foodappgstav7_18.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_counter")
data class OrderCounterEntity(

    @PrimaryKey
    val id: String = "main",

    val orderSerialNo: Long = 0,
    val invoiceSerialNo: Long = 0,
    val kotSerialNo: Long = 0,
    val tokenSerialNo: Long = 0,

    val updatedAt: Long = System.currentTimeMillis()
)