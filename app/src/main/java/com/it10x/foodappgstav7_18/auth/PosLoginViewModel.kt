package com.it10x.foodappgstav7_18.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PosLoginViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _users = MutableStateFlow<List<PosUser>>(emptyList())
    val users: StateFlow<List<PosUser>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {

                _users.value = repository.getPosUsers()

            } catch (e: Exception) {

                _error.value = e.message ?: "Failed to load users."

            } finally {

                _isLoading.value = false

            }
        }
    }

    fun clearError() {
        _error.value = null
    }

}