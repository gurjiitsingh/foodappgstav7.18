package com.it10x.foodappgstav7_18.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_18.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_18.data.pos.entities.PosKotHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KotBatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: PosKotBatchEntity)

    @Query("""
        SELECT * FROM pos_kot_batch
        WHERE tableNo = :tableNo
        ORDER BY createdAt ASC
    """)
    fun getBatchesForTable(tableNo: String): Flow<List<PosKotBatchEntity>>

    @Query("""
        SELECT * FROM pos_kot_batch
        WHERE id = :batchId
        LIMIT 1
    """)
    suspend fun getById(batchId: String): PosKotBatchEntity?

    @Query("""
        DELETE FROM pos_kot_batch
        WHERE tableNo = :tableNo
    """)
    suspend fun clearForTable(tableNo: String)


// =====================================================
// KOT HISTORY
// =====================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(
        items: List<PosKotHistoryEntity>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(
        item: PosKotHistoryEntity
    )



    @Query("""
    SELECT * FROM kot_history
    WHERE batchId = :batchId
    ORDER BY createdAt ASC
""")
    fun getHistoryByBatch(
        batchId: String
    ): Flow<List<PosKotHistoryEntity>>



    @Query("""
SELECT * FROM kot_history
WHERE tableNo = :tableNo
ORDER BY createdAt DESC
""")
    fun getHistoryByTable(
        tableNo: String
    ): Flow<List<PosKotHistoryEntity>>

    @Query("""
SELECT * FROM kot_history
ORDER BY createdAt DESC
""")
    fun getAllHistory(): Flow<List<PosKotHistoryEntity>>

    @Query("""
    SELECT * FROM kot_history
    ORDER BY createdAt DESC
""")


    fun getHistory(): Flow<List<PosKotHistoryEntity>>


    @Query("""
    SELECT * FROM kot_history
    WHERE orderId = :orderId
    ORDER BY createdAt ASC
""")
    fun getHistoryByOrder(
        orderId: String
    ): Flow<List<PosKotHistoryEntity>>





    @Query("""
    UPDATE kot_history
    SET status = :status
    WHERE batchId = :batchId
""")
    suspend fun updateHistoryStatus(
        batchId: String,
        status: String
    )

    @Query("""
    UPDATE kot_history
    SET
        deleted = 1,
        deletedBy = :deletedBy,
        deletedReason = :reason,
        deletedAt = :deletedAt,
        status = 'DELETED'
    WHERE batchId = :batchId
""")
    suspend fun deleteHistoryBatch(
        batchId: String,
        deletedBy: String,
        reason: String,
        deletedAt: Long
    )

    @Query("""
    UPDATE kot_history
    SET
        paidAt = :paidAt,
        orderId = :orderId,
        status = 'PAID'
    WHERE batchId = :batchId
""")
    suspend fun markHistoryPaid(
        batchId: String,
        orderId: String,
        paidAt: Long
    )

    @Query("""
    DELETE FROM kot_history
""")
    suspend fun clearHistory()

    @Query("""
    DELETE FROM kot_history
    WHERE batchId = :batchId
""")
    suspend fun deleteHistoryByBatch(
        batchId: String
    )

    @Query("""
    SELECT COUNT(*)
    FROM kot_history
""")
    suspend fun historyCount(): Int


    @Query("""
UPDATE kot_history
SET
    status = 'DELETED',
    deleted = 1,
    deletedAt = :deletedAt
WHERE id = :itemId
""")
    suspend fun markItemDeleted(
        itemId: String,
        deletedAt: Long
    )




    @Query("""
UPDATE kot_history
SET
    status = 'PAID',
    paidAt = :paidAt,
    orderId = :orderId
WHERE tableNo = :tableNo
AND status != 'DELETED'
""")
    suspend fun markTablePaid(
        tableNo: String,
        orderId: String,
        paidAt: Long
    ): Int

    @Query("""
UPDATE kot_history
SET
    status = 'COMPLIMENTARY',
    paidAt = :complimentaryAt,
      orderId = :orderId
  WHERE tableNo = :tableNo
AND status = 'ACTIVE'
""")
    suspend fun markTableComplimentary(
        tableNo: String,
        orderId: String,
        complimentaryAt: Long
    ): Int





}
