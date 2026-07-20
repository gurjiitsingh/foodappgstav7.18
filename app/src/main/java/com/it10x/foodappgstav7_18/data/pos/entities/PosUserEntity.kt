package com.it10x.foodappgstav7_18.data.pos.entities



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pos_users")
data class PosUserEntity(

    @PrimaryKey
    val userId: String,

    val outletId: String = "",

    val fullName: String,

    val username: String = "",

    val mobile: String = "",

    val employeeId: String = "",

    // admin / manager / cashier / waiter
    val role: String,

    // Temporary for development.
    // Later rename to hashedPassword.
    val password: String,

    val allowPosLogin: Boolean = true,

    val isActive: Boolean = true,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis(),

    // Sync
    val syncStatus: String = "PENDING",

    val lastSyncedAt: Long? = null
)


//
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//
//@Entity(tableName = "pos_users")
//data class PosUserEntity(
//
//    @PrimaryKey
//    val userId: String,          // waiter1, waiter2, main, etc.
//
//    val outletId: String,
//
//    val name: String,            // "Waiter 1"
//    val role: String,            // MAIN / WAITER / CASHIER
//
//    val pinHash: String,         // 🔥 NEVER store plain password
//    val isActive: Boolean = true,
//
//    val createdAt: Long = System.currentTimeMillis(),
//    val updatedAt: Long = System.currentTimeMillis(),
//
//    // Sync
//    val syncStatus: String = "PENDING",
//    val lastSyncedAt: Long? = null
//)
