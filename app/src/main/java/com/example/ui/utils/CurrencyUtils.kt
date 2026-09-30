package com.example.ui.utils

import com.example.data.model.ExchangeRate
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    fun formatCup(amount: Double): String {
        return "${numberFormat.format(amount)} CUP"
    }

    fun formatForeign(amount: Double, symbol: String): String {
        return "$symbol ${numberFormat.format(amount)}"
    }

    fun cupToForeign(amountCup: Double, rate: Double): Double {
        if (rate <= 0.0) return 0.0
        return amountCup / rate
    }

    fun foreignToCup(amountForeign: Double, rate: Double): Double {
        return amountForeign * rate
    }

    fun getRateFor(code: String, rates: List<ExchangeRate>): Double {
        return rates.find { it.currencyCode.equals(code, ignoreCase = true) }?.rateToCup ?: when (code) {
            "USD" -> 330.0
            "MLC" -> 270.0
            "EUR" -> 345.0
            else -> 1.0
        }
    }
}
