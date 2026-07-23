package com.it10x.foodappgstav7_18.ui.dayclosing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_18.data.pos.entities.PosDayClosingEntity
import com.it10x.foodappgstav7_18.data.pos.repository.BusinessDayRepository
import com.it10x.foodappgstav7_18.data.pos.repository.DayClosingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DayClosingViewModel(

    private val businessDayRepository: BusinessDayRepository,
    private val dayClosingRepository: DayClosingRepository

) : ViewModel() {

    private val _uiState = MutableStateFlow(DayClosingUiState())

    val uiState: StateFlow<DayClosingUiState> =
        _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {

        viewModelScope.launch {

            try {

                Log.d("DAY_CLOSE", "Loading Business Day")

                val businessDay =
                    businessDayRepository.getCurrentBusinessDay()

                Log.d("DAY_CLOSE", "Business Day = $businessDay")

                val summary =
                    dayClosingRepository.getSummary(
                        businessDay.businessDate
                    )

                _uiState.value = _uiState.value.copy(

                    businessDate = businessDay.businessDate,

                    openedBy = businessDay.openedByName,

                    openedAt = businessDay.openedAt,

                    openingCash = businessDay.openingCash,

                    totalOrders = summary.totalOrders,

                    totalSales = summary.totalSales,

                    totalDiscount = summary.totalDiscount,

                    totalTax = summary.totalTax,

                    complimentarySales = summary.complimentarySales,

                    cashSales = summary.cashSales,

                    cardSales = summary.cardSales,

                    upiSales = summary.upiSales,

                    walletSales = summary.walletSales,

                    creditSales = summary.creditSales,

                    expectedCash =
                        businessDay.openingCash +
                                summary.cashSales

                )

            } catch (e: Exception) {

                Log.e("DAY_CLOSE", "Load failed", e)

            }
        }
    }

    fun closeBusinessDay() {

        viewModelScope.launch {

            try {

                Log.d("DAY_CLOSE", "Starting Day Closing")

                // ----------------------------
                // Load Current Business Day
                // ----------------------------
                val businessDay =
                    businessDayRepository.getCurrentBusinessDay()

                // ----------------------------
                // Already Closed?
                // ----------------------------
                if (
                    dayClosingRepository.alreadyClosed(
                        businessDay.businessDate
                    )
                ) {

                    Log.d(
                        "DAY_CLOSE",
                        "Business Day Already Closed"
                    )

                    return@launch
                }

                // ----------------------------
                // Load Latest Summary
                // ----------------------------
                val summary =
                    dayClosingRepository.getSummary(
                        businessDay.businessDate
                    )

                // ----------------------------
                // Cash Calculation
                // ----------------------------
                val actualCash =
                    _uiState.value.actualCash.toDoubleOrNull()
                        ?: 0.0

                val expectedCash =
                    businessDay.openingCash +
                            summary.cashSales

                val difference =
                    actualCash - expectedCash

                // ----------------------------
                // Create Closing Record
                // ----------------------------
                val dayClosing = PosDayClosingEntity(

                    id = businessDay.businessDate,

                    businessDate = businessDay.businessDate,

                    openedAt = businessDay.openedAt,

                    closedAt = System.currentTimeMillis(),

                    openedById = businessDay.openedById,

                    openedByName = businessDay.openedByName,

                    // TODO:
                    // Replace with logged-in user
                    closedById = businessDay.openedById,

                    closedByName = businessDay.openedByName,

                    openingCash = businessDay.openingCash,

                    expectedCash = expectedCash,

                    actualCash = actualCash,

                    cashDifference = difference,

                    totalSales = summary.totalSales,

                    totalRefund = 0.0,

                    totalDiscount = summary.totalDiscount,

                    totalTax = summary.totalTax,

                    cashSales = summary.cashSales,

                    cardSales = summary.cardSales,

                    upiSales = summary.upiSales,

                    walletSales = summary.walletSales,

                    creditSales = summary.creditSales,

                    complimentarySales = summary.complimentarySales,

                    totalOrders = summary.totalOrders,

                    syncStatus = "PENDING",

                    createdAt = System.currentTimeMillis()
                )

                // ----------------------------
                // Save Day Closing
                // ----------------------------
                dayClosingRepository.save(dayClosing)

                Log.d(
                    "DAY_CLOSE",
                    "Day Closing Saved"
                )

                // ----------------------------
                // Close Current Business Day
                // ----------------------------
                businessDayRepository.closeCurrentBusinessDay(

                    closedById = businessDay.openedById,

                    closedByName = businessDay.openedByName
                )

                Log.d(
                    "DAY_CLOSE",
                    "Business Day Closed"
                )

                // ----------------------------
                // Create Next Business Day
                // ----------------------------
                businessDayRepository.createNextBusinessDay(

                    openingCash = actualCash,

                    openedById = businessDay.openedById,

                    openedByName = businessDay.openedByName
                )

                Log.d(
                    "DAY_CLOSE",
                    "Next Business Day Created"
                )

                // ----------------------------
                // Refresh Screen
                // ----------------------------
                loadData()

            } catch (e: Exception) {

                Log.e(
                    "DAY_CLOSE",
                    "Day Closing Failed",
                    e
                )
            }
        }
    }

    fun updateActualCash(value: String) {

        _uiState.value =
            _uiState.value.copy(
                actualCash = value
            )
    }

    fun updateNotes(value: String) {

        _uiState.value =
            _uiState.value.copy(
                notes = value
            )
    }
}