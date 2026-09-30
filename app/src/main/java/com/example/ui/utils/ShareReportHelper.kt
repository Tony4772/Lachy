package com.example.ui.utils

import android.content.Context
import android.content.Intent
import com.example.data.model.PurchaseRecord
import com.example.data.model.StockItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareReportHelper {

    private val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun generateLachyReport(
        purchasesEnCamino: List<PurchaseRecord>,
        criticalItems: List<StockItem>,
        allItems: List<StockItem>,
        usdRate: Double
    ): String {
        val sb = StringBuilder()
        val totalInventoryValueCup = allItems.sumOf { it.totalValueCup }
        val totalInventoryValueUsd = if (usdRate > 0) totalInventoryValueCup / usdRate else 0.0

        sb.appendLine("📊 REPORTE DE INVENTARIO & ALMACÉN - CONTROL LACHY")
        sb.appendLine("📅 Fecha: ${timeFormat.format(Date())}")
        sb.appendLine("💰 Valor total en almacén: ${CurrencyUtils.formatCup(totalInventoryValueCup)} (≈ $ ${String.format(Locale.US, "%.2f", totalInventoryValueUsd)} USD)")
        sb.appendLine("═══════════════════════════════════")

        // 1. Compras en camino
        sb.appendLine("🚚 COMPRAS REALIZADAS (EN TRÁNSITO AL ALMACÉN):")
        if (purchasesEnCamino.isEmpty()) {
            sb.appendLine("• Sin compras en camino actualmente.")
        } else {
            purchasesEnCamino.forEach { p ->
                sb.appendLine("• ${p.quantity} ${p.unit} de ${p.itemName}")
                sb.appendLine("  - Precio: ${CurrencyUtils.formatCup(p.unitPriceCup)}/${p.unit} (Total: ${CurrencyUtils.formatCup(p.totalCostCup)})")
                sb.appendLine("  - Comprado por: ${p.buyerName} en ${p.purchasePlace}")
                if (p.notes.isNotBlank()) sb.appendLine("  - Nota: ${p.notes}")
            }
        }
        sb.appendLine("───────────────────────────────────")

        // 2. Faltantes críticos
        sb.appendLine("⚠️ PRODUCTOS CON STOCK BAJO O CRÍTICO:")
        if (criticalItems.isEmpty()) {
            sb.appendLine("• Todos los productos están en nivel óptimo.")
        } else {
            criticalItems.forEach { item ->
                sb.appendLine("• ${item.name}: Quedan ${item.currentStock} ${item.unit} (Mínimo: ${item.minStockAlert})")
            }
        }
        sb.appendLine("───────────────────────────────────")

        // 3. Stock general
        sb.appendLine("📦 EXISTENCIAS EN ALMACÉN:")
        val grouped = allItems.groupBy { it.category }
        grouped.forEach { (category, items) ->
            sb.appendLine("[$category]")
            items.forEach { item ->
                val statusIcon = when {
                    item.isOutOfStock -> "🔴"
                    item.isCritical -> "🟡"
                    else -> "🟢"
                }
                sb.appendLine("$statusIcon ${item.name}: ${item.currentStock} ${item.unit} (Val: ${CurrencyUtils.formatCup(item.totalValueCup)})")
            }
        }

        sb.appendLine("═══════════════════════════════════")
        sb.appendLine("Generado por Control Lachy (Inventarios)")
        return sb.toString()
    }

    fun shareText(context: Context, title: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, title)
        context.startActivity(chooser)
    }
}
