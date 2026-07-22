package com.it10x.foodappgstav7_18.data.pos.repository

import com.it10x.foodappgstav7_18.data.pos.dao.BusinessDayDao
import com.it10x.foodappgstav7_18.data.pos.entities.PosBusinessDayEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusinessDayRepository(
    private val businessDayDao: BusinessDayDao
) {

    /**
     * Returns the current business day.
     * If none exists (first app launch), creates one automatically.
     */
    suspend fun getCurrentBusinessDay(): PosBusinessDayEntity {

        val existing = businessDayDao.getCurrentBusinessDay()

        if (existing != null) {
            return existing
        }

        val now = System.currentTimeMillis()

        val today = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date(now))

        val firstDay = PosBusinessDayEntity(
            businessDate = today,
            openedAt = now,
            openedById = "",
            openedByName = "",
            isClosed = false,
            updatedAt = now
        )

        businessDayDao.save(firstDay)

        return firstDay
    }

    /**
     * Returns only the business date.
     */
    suspend fun getBusinessDate(): String {
        return getCurrentBusinessDay().businessDate
    }

    /**
     * Opens the next business day.
     * Call this ONLY after a successful day closing.
     */
    suspend fun openNextBusinessDay(
        openedById: String,
        openedByName: String
    ) {

        val now = System.currentTimeMillis()

        val nextDate = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date(now))

        val nextBusinessDay = PosBusinessDayEntity(
            id = "CURRENT",
            businessDate = nextDate,
            openedAt = now,
            openedById = openedById,
            openedByName = openedByName,
            isClosed = false,
            updatedAt = now
        )

        businessDayDao.save(nextBusinessDay)
    }
}