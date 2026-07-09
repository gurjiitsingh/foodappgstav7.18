package com.it10x.foodappgstav7_18.network.fiskaly

object TokenManager {

    private var token: String? = null

    fun saveToken(newToken: String) {
        token = newToken
    }

    fun getToken(): String? = token
}