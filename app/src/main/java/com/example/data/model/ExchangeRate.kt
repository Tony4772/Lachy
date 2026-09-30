package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRate(
    @PrimaryKey
    val currencyCode: String, // "USD", "MLC", "EUR"
    val rateToCup: Double,    // e.g. 330.0 for 1 USD = 330 CUP
    val symbol: String,       // "$", "MLC", "€"
    val updatedAt: Long = System.currentTimeMillis()
)
