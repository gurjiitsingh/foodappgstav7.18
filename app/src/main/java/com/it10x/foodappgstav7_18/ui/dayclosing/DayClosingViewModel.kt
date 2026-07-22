package com.it10x.foodappgstav7_18.ui.dayclosing


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

            val businessDay =
                businessDayRepository.getCurrentBusinessDay()

            _uiState.value = _uiState.value.copy(

                businessDate = businessDay.businessDate,

                openedBy = businessDay.openedByName,

                openedAt = businessDay.openedAt,

                openingCash = businessDay.openingCash

            )

            // Remaining totals will be loaded next.
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