package com.it10x.foodappgstav7_18.data.pos.repository

import com.it10x.foodappgstav7_18.data.pos.dao.BusinessDayDao
import com.it10x.foodappgstav7_18.data.pos.entities.PosBusinessDayEntity

import java.util.Date

import java.text.SimpleDateFormat
import java.util.Calendar
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
            id = "CURRENT",
            businessDate = today,
            openedAt = now,
            openedById = "",
            openedByName = "",
            openingCash = 0.0,
            isClosed = false,
            status = "OPEN",
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
            openingCash = 0.0,
            isClosed = false,
            status = "OPEN",
            updatedAt = now
        )

        businessDayDao.save(nextBusinessDay)
    }


    suspend fun closeCurrentBusinessDay(
        closedById: String,
        closedByName: String
    ) {

        val current = getCurrentBusinessDay()

        val closed = current.copy(
            isClosed = true,
            status = "CLOSED",
            closedAt = System.currentTimeMillis(),
            closedById = closedById,
            closedByName = closedByName,
            updatedAt = System.currentTimeMillis()
        )

        businessDayDao.save(closed)
    }



    suspend fun createNextBusinessDay(
        openingCash: Double,
        openedById: String,
        openedByName: String
    ) {

        val current = getCurrentBusinessDay()

        val sdf = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        val calendar = Calendar.getInstance()

        calendar.time = sdf.parse(current.businessDate)!!

        // Move to next business day
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        val nextDate = sdf.format(calendar.time)

        val now = System.currentTimeMillis()

        val nextDay = PosBusinessDayEntity(
            id = "CURRENT",
            businessDate = nextDate,
            openedAt = now,
            openedById = openedById,
            openedByName = openedByName,
            openingCash = openingCash,
            isClosed = false,
            status = "OPEN",
            updatedAt = now
        )

        businessDayDao.save(nextDay)
    }
}