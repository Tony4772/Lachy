package com.example

import com.example.data.model.PurchaseRecord
import com.example.data.model.PurchaseStatus
import com.example.data.model.StockItem
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
    fun testStockItem_isCritical() {
        val cheese = StockItem(
            id = 1L,
            name = "Queso Barra",
            category = "Insumos",
            unit = "Libras",
            currentStock = 12.0,
            minStockAlert = 20.0
        )
        assertTrue(cheese.isCritical)
    }

    @Test
    fun testStockItem_totalValue() {
        val flour = StockItem(
            id = 2L,
            name = "Harina de Trigo",
            category = "Insumos",
            unit = "Libras",
            currentStock = 50.0,
            lastUnitPriceCup = 200.0
        )
        assertEquals(10000.0, flour.totalValueCup, 0.001)
    }

    @Test
    fun testLachyReportGeneration() {
        val purchasesEnCamino = listOf(
            PurchaseRecord(
                id = 1L,
                itemName = "Queso Barra",
                quantity = 25.0,
                unit = "Libras",
                unitPriceCup = 670.0,
                totalCostCup = 16750.0,
                buyerName = "Carlos",
                purchasePlace = "Mercado Agro Cienfuegos",
                status = PurchaseStatus.COMPRADO_EN_CAMINO
            )
        )
        val criticalItems = listOf(
            StockItem(
                id = 2L,
                name = "Cajas Termopack",
                category = "Empaques",
                unit = "Termopacks",
                currentStock = 18.0,
                minStockAlert = 60.0
            )
        )
        val allItems = listOf(
            StockItem(
                id = 3L,
                name = "Moldes de Pizza 30cm",
                category = "Equipos y Utensilios",
                unit = "Moldes",
                currentStock = 24.0,
                minStockAlert = 20.0,
                lastUnitPriceCup = 1200.0
            )
        )

        val report = ShareReportHelper.generateLachyReport(purchasesEnCamino, criticalItems, allItems, 330.0)
        assertTrue(report.contains("REPORTE DE INVENTARIO & ALMACÉN"))
        assertTrue(report.contains("Queso Barra"))
        assertTrue(report.contains("670 CUP"))
        assertTrue(report.contains("Moldes de Pizza 30cm"))
        assertTrue(report.contains("Cajas Termopack"))
    }
}
