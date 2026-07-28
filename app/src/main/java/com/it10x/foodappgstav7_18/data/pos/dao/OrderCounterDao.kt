package com.it10x.foodappgstav7_18.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_18.data.pos.entities.OrderCounterEntity

@Dao
interface OrderCounterDao {

    @Query("SELECT * FROM order_counter WHERE id='main'")
    suspend fun getCounter(): OrderCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(counter: OrderCounterEntity)

    @Query("UPDATE order_counter SET orderSerialNo=:serialNo, updatedAt=:updatedAt WHERE id='main'")
    suspend fun updateLastOrderSerialNo(
        serialNo: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
}