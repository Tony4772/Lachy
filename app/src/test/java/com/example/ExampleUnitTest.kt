package com.example

import com.example.data.model.ExchangeRate
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItem
import com.example.data.model.OrderWithItems
import com.example.ui.utils.CurrencyUtils
import com.example.ui.utils.ShareReportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCurrencyConversion_CupToUsd() {
        val amountCup = 3300.0
        val rateUsd = 330.0
        val usd = CurrencyUtils.cupToForeign(amountCup, rateUsd)
        assertEquals(10.0, usd, 0.001)
    }

    @Test
    fun testCurrencyConversion_ForeignToCup() {
        val usdAmount = 25.0
        val rateUsd = 330.0
        val cup = CurrencyUtils.foreignToCup(usdAmount, rateUsd)
        assertEquals(8250.0, cup, 0.001)
    }

    @Test
    fun testTicketGeneration() {
        val order = OrderEntity(
            id = 1L,
            tableId = 1L,
            tableName = "Mesa 2",
            waiterName = "José",
            subtotalCup = 2000.0,
            servicePercent = 10.0,
            totalCup = 2200.0
        )
        val items = listOf(
            OrderItem(
                id = 1L,
                orderId = 1L,
                menuItemId = 1L,
                name = "Ropa Vieja Criolla",
                category = "Platos Fuertes",
                priceCup = 1450.0,
                quantity = 1
            ),
            OrderItem(
                id = 2L,
                orderId = 1L,
                menuItemId = 2L,
                name = "Cerveza Cristal",
                category = "Bebidas",
                priceCup = 450.0,
                quantity = 1
            )
        )
        val orderWithItems = OrderWithItems(order, items)
        val rates = listOf(
            ExchangeRate("USD", 330.0, "$"),
            ExchangeRate("MLC", 270.0, "MLC"),
            ExchangeRate("EUR", 345.0, "€")
        )

        val ticket = ShareReportHelper.generateOrderTicketText(orderWithItems, rates)
        assertTrue(ticket.contains("PALADAR LACHY"))
        assertTrue(ticket.contains("Ropa Vieja Criolla"))
        assertTrue(ticket.contains("Cerveza Cristal"))
        assertTrue(ticket.contains("Servicio (10%)"))
    }
}
