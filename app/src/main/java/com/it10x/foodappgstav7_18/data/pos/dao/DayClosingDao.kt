package com.it10x.foodappgstav7_18.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.it10x.foodappgstav7_18.data.pos.entities.PosDayClosingEntity

@Dao
interface DayClosingDao {

    @Insert
    suspend fun insert(dayClosing: PosDayClosingEntity)

    @Query("""
        SELECT *
        FROM pos_day_closing
        ORDER BY closedAt DESC
    """)
    suspend fun getAll(): List<PosDayClosingEntity>



    @Query("""
SELECT *
FROM pos_day_closing
WHERE businessDate = :businessDate
LIMIT 1
""")
    suspend fun getByBusinessDate(
        businessDate: String
    ): PosDayClosingEntity?
}