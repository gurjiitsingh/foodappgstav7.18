package com.it10x.foodappgstav7_18.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_18.data.pos.dao.KotBatchDao
import com.it10x.foodappgstav7_18.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_18.data.pos.dao.TableDao
import com.it10x.foodappgstav7_18.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_18.data.pos.entities.PosKotHistoryEntity
import com.it10x.foodappgstav7_18.data.pos.entities.PosKotItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class KotRepository(
    private val batchDao: KotBatchDao,
    private val kotItemDao: KotItemDao,
    private val tableDao: TableDao
) {




    suspend fun insertItemsInBill(
        tableNo: String,
        items: List<PosKotItemEntity>,
        role: String
    ) {



        kotItemDao.insertAll(items)


     //  Log.d("KOT_DEBUG", "insertAll() executed")
    }



    suspend fun deleteKotByTable(tableId: String) {
        kotItemDao.deleteByTableId(tableId)
        syncBillCounters(tableId)
    }

    suspend fun markDoneAll(tableNo: String) {
        kotItemDao.markAllDone(tableNo)
         }
    suspend fun markPrinted(tableNo: String) {
        kotItemDao.markAllPrinted(tableNo)
    }

    private suspend fun syncBillCounters(tableNo: String) {
       // val billCount = kotItemDao.countDoneItems(tableNo) ?: 0
        val billCount = kotItemDao.getBillQtyCount(tableNo) ?: 0
        val billAmount = kotItemDao.sumDoneAmount(tableNo) ?: 0.0

        tableDao.updateBill(tableNo, billCount, billAmount)
    }

    private suspend fun syncKitchenCount(tableNo: String) {
        val count = kotItemDao.countBillDone(tableNo) ?: 0
        tableDao.setKitchenCount(tableNo, count)
    }

    suspend fun syncBillCount(tableNo: String) {
          syncBillCounters(tableNo)
    }

    //THIS FUNCITON IS CALLED IN TABLE GRID
    suspend fun syncKinchenCount(tableNo: String) {
            syncKitchenCount(tableNo)
    }


    suspend fun transferTable(oldTableId: String, newTableId: String) {

        if (oldTableId == newTableId) return

        try {

            // 1️⃣ Move all KOT items to new table
            kotItemDao.transferTable(oldTableId, newTableId)

            // 2️⃣ Refresh counters for old table
            syncKinchenCount(oldTableId)
            syncBillCount(oldTableId)

            // 3️⃣ Refresh counters for new table
            syncKinchenCount(newTableId)
            syncBillCount(newTableId)

            Log.d("TABLE_TRANSFER", "Moved KOT from $oldTableId → $newTableId")

        } catch (e: Exception) {

            Log.e("TABLE_TRANSFER", "Transfer failed", e)

        }
    }


  // =====================================================
// KOT HISTORY
// =====================================================
    suspend fun saveHistory(
        batch: PosKotBatchEntity,
        items: List<PosKotItemEntity>,
        source: String
    ) {

        val historyItems = items.map { item ->

            PosKotHistoryEntity(
                id = item.id,
                batchId = batch.id,
                sessionId = batch.sessionId ?: "",
                tableNo = batch.tableNo ?: "",
                orderType = batch.orderType,

                productId = item.productId,
                name = item.name,
                quantity = item.quantity,
                note = item.note,
                modifiersJson = item.modifiersJson,
                modifierTotal = item.modifierTotal,

                createdAt = item.createdAt,

                source = source,
                status = "ACTIVE",

                deleted = false,
                deletedBy = null,
                deletedReason = null,
                deletedAt = null,

                deviceId = batch.deviceId ?: "",
                deviceName = batch.deviceName,

                paidAt = null,
                orderId = null
            )
        }

        batchDao.insertHistory(historyItems)
    }





    fun getKotHistory(): Flow<List<PosKotHistoryEntity>> {
        return batchDao.getHistory()
    }



    fun getHistoryAll(

    ): Flow<List<PosKotHistoryEntity>> {
        return batchDao.getAllHistory()
    }

    suspend fun markHistoryDeleted(
        itemId: String
    ) {

        batchDao.markItemDeleted(
            itemId = itemId,
            deletedAt = System.currentTimeMillis()
        )
    }


    suspend fun markHistoryComplimentary(
        tableNo: String,
        orderId: String,
        reason: String
    ) {

        val rows = batchDao.markTableComplimentary(
            tableNo = tableNo,
            orderId = orderId,

            complimentaryAt = System.currentTimeMillis()
        )


    }

    suspend fun markHistoryPaid(
        tableNo: String,
        orderId: String
    ) {

        val rows = batchDao.markTablePaid(
            tableNo = tableNo,
            orderId = orderId,
            paidAt = System.currentTimeMillis()
        )

        Log.d(
            "KOT_HISTORY",
            "Marked PAID rows=$rows table=$tableNo order=$orderId"
        )
    }

}
