package com.it10x.foodappgstav7_18.ui.kot.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_18.data.pos.entities.PosKotHistoryEntity
import com.it10x.foodappgstav7_18.data.pos.repository.KotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KotHistoryViewModel(
    private val repository: KotRepository,

) : ViewModel() {

    private val _history =
        MutableStateFlow<List<PosKotHistoryEntity>>(emptyList())

    val history: StateFlow<List<PosKotHistoryEntity>> = _history

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repository.getHistoryAll().collect { history ->
                _history.value = history
            }
        }
    }
}