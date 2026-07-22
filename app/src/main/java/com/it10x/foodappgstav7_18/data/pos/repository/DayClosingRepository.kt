package com.it10x.foodappgstav7_18.data.pos.repository

import com.it10x.foodappgstav7_18.data.pos.dao.DayClosingDao
import com.it10x.foodappgstav7_18.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_18.data.pos.dao.PosOrderPaymentDao
import com.it10x.foodappgstav7_18.data.pos.entities.PosDayClosingEntity

class DayClosingRepository(
    private val dao: DayClosingDao,
    private val orderMasterDao: OrderMasterDao,
    private val paymentDao: PosOrderPaymentDao
) {

    suspend fun save(
        entity: PosDayClosingEntity
    ) {
        dao.insert(entity)
    }

    suspend fun history(): List<PosDayClosingEntity> {
        return dao.getAll()
    }

    suspend fun getSummary(
        businessDate: String
    ): DayClosingSummary {

        val totalSales =
            orderMasterDao.getTotalSales(businessDate)

        val totalOrders =
            orderMasterDao.getOrderCount(businessDate)

        val totalDiscount =
            orderMasterDao.getTotalDiscount(businessDate)

        val totalTax =
            orderMasterDao.getTotalTax(businessDate)

        val complimentarySales =
            orderMasterDao.getComplimentarySales(businessDate)

        val cashSales =
            paymentDao.getPaymentTotal(
                mode = "CASH",
                businessDate = businessDate
            )

        val cardSales =
            paymentDao.getPaymentTotal(
                mode = "CARD",
                businessDate = businessDate
            )

        val upiSales =
            paymentDao.getPaymentTotal(
                mode = "UPI",
                businessDate = businessDate
            )

        val walletSales =
            paymentDao.getPaymentTotal(
                mode = "WALLET",
                businessDate = businessDate
            )

        val creditSales =
            orderMasterDao.getCreditSales(businessDate)

        return DayClosingSummary(
            totalOrders = totalOrders,
            totalSales = totalSales,
            totalDiscount = totalDiscount,
            totalTax = totalTax,
            complimentarySales = complimentarySales,
            cashSales = cashSales,
            cardSales = cardSales,
            upiSales = upiSales,
            walletSales = walletSales,
            creditSales = creditSales
        )
    }
}

data class DayClosingSummary(

    val totalOrders: Int,

    val totalSales: Double,

    val totalDiscount: Double,

    val totalTax: Double,

    val complimentarySales: Double,

    val cashSales: Double,

    val cardSales: Double,

    val upiSales: Double,

    val walletSales: Double,

    val creditSales: Double
)