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

        // Current business day
        val currentBusinessDate =
            sdf.parse(current.businessDate)!!

        // Real today
        val todayCalendar =
            Calendar.getInstance()

        val todayDate =
            sdf.parse(
                sdf.format(todayCalendar.time)
            )!!

        // Candidate = current business day + 1
        val nextCalendar =
            Calendar.getInstance()

        nextCalendar.time =
            currentBusinessDate

        nextCalendar.add(
            Calendar.DAY_OF_MONTH,
            1
        )

        val candidateDate =
            nextCalendar.time

        /*
           Rules

           Today = 01 Aug
           Business = 01 Aug
           -> Next = 02 Aug

           Today = 01 Aug
           Business = 02 Aug
           -> STOP (already prepared tomorrow)

           Today = 02 Aug
           Business = 01 Aug
           -> Next = 02 Aug

           Today = 06 Aug
           Business = 01 Aug
           -> Next = 06 Aug
        */

        val nextDate = when {

            // Already created tomorrow
            candidateDate.after(todayDate) -> {
                throw Exception(
                    "Business day already closed for today."
                )
            }

            // Forgot to close for one or more days
            currentBusinessDate.before(todayDate) -> {
                sdf.format(todayDate)
            }

            // Normal case
            else -> {
                sdf.format(candidateDate)
            }
        }

        val now =
            System.currentTimeMillis()

        val nextDay =
            PosBusinessDayEntity(

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