package com.it10x.foodappgstav7_18.data.online.sync

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.it10x.foodappgstav7_18.core.PosRole
import com.it10x.foodappgstav7_18.data.pos.KotProcessor
import com.it10x.foodappgstav7_18.data.pos.dao.ProcessedCloudOrderDao
import com.it10x.foodappgstav7_18.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_18.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_18.data.pos.entities.ProcessedCloudOrderEntity
import com.it10x.foodappgstav7_18.ui.kitchen.KitchenViewModel
import com.it10x.foodappgstav7_18.ui.waiterkitchen.WaiterKitchenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class GlobalOrderSyncManager(
    private val firestore: FirebaseFirestore,
    private val processedDao: ProcessedCloudOrderDao,
    private val kitchenViewModel: KitchenViewModel,
   // private val waiterkitchenViewModel: WaiterKitchenViewModel,
    private val role: PosRole
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private var mainPosListener: ListenerRegistration? = null
    private var waiterListener: ListenerRegistration? = null
    private val tableLocks =
        java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.sync.Mutex>()



    private val lastProcessedMap = ConcurrentHashMap<String, Long>()



    // -------------------- START LISTENERS --------------------

    fun startListening() {
        Log.d("KOT_DEBUG", "startListening called: role=$role")

        stopListening() // always stop first

        // Cleanup once on start
        cleanupOldProcessedIds()

        when (role) {
            PosRole.MAIN -> startMainPosListener()
            PosRole.WAITER -> startWaiterListener()
        }
    }

    fun stopListening() {
        mainPosListener?.remove()
        mainPosListener = null

        waiterListener?.remove()
        waiterListener = null

        Log.d("SYNC", "All Firestore listeners stopped")
    }


private fun startMainPosListener() {
    Log.d("KOT_DEBUG", "startMainPosListener called: role=MAIN")

    // Stop previous listener if any
    mainPosListener?.remove()
    mainPosListener = null

    val cutoff = System.currentTimeMillis() - (6 * 60 * 60 * 1000) // 6 hours

    mainPosListener = firestore.collection("waiter_orders")
        .addSnapshotListener { snapshot, error ->

            if (error != null || snapshot == null) return@addSnapshotListener

            snapshot.documentChanges.forEach { change ->
                val orderDoc = change.document
                val orderId = orderDoc.id
                val createdAt = orderDoc.getLong("createdAt")

                if (createdAt == null) {
                    Log.w("SYNC", "Missing createdAt → skip: $orderId")
                    return@forEach
                }

                val orderRef = firestore.collection("waiter_orders").document(orderId)

                // ---------------- CLEANUP OLD ORDERS ----------------
                if (createdAt < cutoff) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val itemsSnapshot = orderRef.collection("items").get().await()
                            val batch = firestore.batch()
                            itemsSnapshot.documents.forEach { batch.delete(it.reference) }
                            batch.delete(orderRef)
                            batch.commit().await()
                            Log.d("CLEANUP", "🧹 Deleted old order: $orderId")
                        } catch (e: Exception) {
                            Log.e("CLEANUP", "❌ Failed deleting old order: $orderId", e)
                        }
                    }
                    return@forEach
                }

                // ---------------- PROCESS NEW ORDERS ----------------
                // Only process newly added orders
                if (change.type != DocumentChange.Type.ADDED) return@forEach

                val tableNo = orderDoc.getString("tableNo") ?: ""
                val sessionId = orderDoc.getString("sessionId") ?: ""
                val source = orderDoc.getString("source") ?: "UNKNOWN"

                scope.launch(Dispatchers.IO) {
                    try {
                        // Prevent double-processing
                        val insertResult = processedDao.insert(
                            ProcessedCloudOrderEntity(
                                orderId = orderId,
                                processedAt = System.currentTimeMillis()
                            )
                        )
                        if (insertResult == -1L) {
                            Log.d("SYNC", "Already processed: $orderId")
                            return@launch
                        }

                        // Fetch order items
                        val itemsSnapshot = orderRef.collection("items").get().await()
                        val cartList = itemsSnapshot.documents.map { itemDoc ->
                            PosCartEntity(
                                sessionId = sessionId,
                                tableId = tableNo,
                                productId = itemDoc.getString("productId") ?: "",
                                name = itemDoc.getString("productName") ?: "",
                                productMode =
                                    itemDoc.getString("productMode")
                                        ?: "raw_stock",
                                currentStock =
                                    itemDoc.getDouble("currentStock")
                                        ?: 0.0,
                                categoryId = itemDoc.getString("categoryId") ?: "",
                                categoryName = itemDoc.getString("categoryName") ?: "",
                                parentId = null,
                                isVariant = false,
                                basePrice = itemDoc.getDouble("price") ?: 0.0,
                                modifierTotal = 0.0,
                                quantity = (itemDoc.getLong("quantity") ?: 1L).toInt(),
                                taxRate = itemDoc.getDouble("taxRate") ?: 0.0,
                                taxType = "exclusive",
                                note = itemDoc.getString("note") ?: "",
                                modifiersJson = itemDoc.getString("modifiersJson") ?: "",
                                kitchenPrintReq = itemDoc.getBoolean("kitchenPrintReq") ?: true,
                                createdAt = System.currentTimeMillis()
                            )
                        }

                        if (cartList.isEmpty()) return@launch

                        // Save to kitchen
                        kitchenViewModel.saveKotFromFirestoreWaiter(
                            orderType = "DINE_IN",
                            sessionId = sessionId,
                            tableNo = tableNo,
                            cartItems = cartList,
                            deviceId = "WAITER",
                            deviceName = "WAITER",
                            appVersion = "WAITER",
                            role = "FIRESTORE",
                            source = "FIRESTORE"
                        )

                        // Delete after processing
                        val batch = firestore.batch()
                        itemsSnapshot.documents.forEach { batch.delete(it.reference) }
                        batch.delete(orderRef)
                        batch.commit().await()



                        Log.d("SYNC", "✅ Processed & deleted: $orderId")
                    } catch (e: Exception) {
                        Log.e("SYNC", "❌ Error processing order: $orderId", e)
                    }
                }



            }
        }
}
    // -------------------- WAITER --------------------
    // Listen to only MAIN POS orders

private fun startWaiterListener() {
        Log.d("TABLE_SYNC", "✅ waiter is runn on time")
        waiterListener?.remove()
        waiterListener = null

        waiterListener = firestore
            .collection("pos_tables")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("SYNC", "❌ Listener error", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                for (change in snapshot.documentChanges) {

                    val doc = change.document

                    val tableId = doc.id
                    val source = doc.getString("source") ?: "UNKNOWN"
                    val sessionId = doc.getString("sessionId") ?: tableId
                    val status = doc.getString("status") ?: "UNKNOWN"
                    val active = doc.getBoolean("active") ?: false
                    val updatedAt = doc.getLong("updatedAt") ?: 0L

                    val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()

                    Log.d("SYNC_FLOW", "🚀 Listener hit → source=$source")

                    // 🔥 LOGGING
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d("SYNC", "🆕 TABLE ADDED → $tableId")
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d("SYNC", "✏️ TABLE UPDATED → $tableId")
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d("SYNC", "❌ TABLE REMOVED → $tableId")
                        }
                    }

                    if (items.isEmpty()) {
                        Log.d("SYNC", "🪹 No items in table")
                    } else {
//                        items.forEachIndexed { index, item ->
//                            Log.d(
//                                "SYNC",
//                                "🍽 Item[$index] → ${item["name"]} | Qty: ${item["quantity"]} | Price: ${item["price"]}"
//                            )
//                        }
                    }

                    // ✅ ONLY PROCESS ADDED / MODIFIED
                    if (change.type != DocumentChange.Type.ADDED &&
                        change.type != DocumentChange.Type.MODIFIED
                    ) continue


                    scope.launch(Dispatchers.IO) {

                        val mutex = tableLocks.getOrPut(tableId) {
                            kotlinx.coroutines.sync.Mutex()
                        }

                        mutex.lock()

                        try {

                            val lastUpdated = lastProcessedMap[tableId] ?: 0L

                            if (updatedAt <= lastUpdated) {
                                Log.d(
                                    "SYNC_ORDER",
                                    "⏭️ Ignoring stale update table=$tableId updatedAt=$updatedAt last=$lastUpdated"
                                )
                                return@launch
                            }

                            Log.d(
                                "SYNC_ORDER",
                                "ACCEPT table=$tableId updatedAt=$updatedAt previous=${lastUpdated}"
                            )

                            val uniqueId = "${tableId}_$updatedAt"

                            val insertResult = processedDao.insert(
                                ProcessedCloudOrderEntity(
                                    orderId = uniqueId,
                                    processedAt = System.currentTimeMillis()
                                )
                            )

                            if (insertResult == -1L) {
                                Log.d("SYNC", "⏭️ Already processed: $uniqueId")
                                return@launch
                            }

                            Log.d(
                                "SYNC_ORDER",
                                "PROCESSING table=$tableId updatedAt=$updatedAt items=${items.size}"
                            )

                            kitchenViewModel.replaceKotFromFirestoreWaiterListener(
                                tableId = tableId,
                                sessionId = sessionId,
                                items = items,
                                source = "FIRESTORE"
                            )

// ✅ Mark processed ONLY after successful import
                            lastProcessedMap[tableId] = updatedAt

                            Log.d(
                                "SYNC_ORDER",
                                "DONE table=$tableId updatedAt=$updatedAt"
                            )


                        } catch (e: Exception) {

                            Log.e("SYNC", "❌ Sync failed for table: $tableId", e)

                        } finally {

                            mutex.unlock()
                        }
                    }


                }
            }
    }

    // -------------------- ORDER PROCESSING --------------------

    private fun cleanupOldProcessedIds() {
        scope.launch(Dispatchers.IO) {
            try {
                val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 24h
                val deleted = processedDao.deleteOlderThan(cutoff)
                Log.d("CLEANUP", "🧹 Deleted $deleted old processed IDs")
            } catch (e: Exception) {
                Log.e("CLEANUP", "❌ Failed to cleanup processed IDs", e)
            }
        }
    }
}