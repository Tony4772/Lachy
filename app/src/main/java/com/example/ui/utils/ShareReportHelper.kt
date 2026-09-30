package com.example.ui.utils

import android.content.Context
import android.content.Intent
import com.example.data.model.ExchangeRate
import com.example.data.model.Expense
import com.example.data.model.OrderItem
import com.example.data.model.OrderWithItems
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareReportHelper {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun generateOrderTicketText(
        orderWithItems: OrderWithItems,
        rates: List<ExchangeRate>
    ): String {
        val order = orderWithItems.order
        val items = orderWithItems.items
        val usdRate = CurrencyUtils.getRateFor("USD", rates)
        val mlcRate = CurrencyUtils.getRateFor("MLC", rates)
        val eurRate = CurrencyUtils.getRateFor("EUR", rates)

        val usdEquiv = CurrencyUtils.cupToForeign(order.totalCup, usdRate)
        val mlcEquiv = CurrencyUtils.cupToForeign(order.totalCup, mlcRate)
        val eurEquiv = CurrencyUtils.cupToForeign(order.totalCup, eurRate)

        val sb = StringBuilder()
        sb.appendLine("══════════════════════════")
        sb.appendLine("🍽️ PALADAR LACHY - CIENFUEGOS 🇨🇺")
        sb.appendLine("Comanda / Cuenta")
        sb.appendLine("Mesa: ${order.tableName}")
        sb.appendLine("Atendido por: ${order.waiterName}")
        sb.appendLine("Fecha: ${dateFormat.format(Date(order.openedAt))}")
        sb.appendLine("──────────────────────────")

        items.forEach { item ->
            val noteStr = if (item.notes.isNotBlank()) " (${item.notes})" else ""
            sb.appendLine("${item.quantity}x ${item.name}$noteStr")
            sb.appendLine("   ${CurrencyUtils.formatCup(item.totalCup)}")
        }

        sb.appendLine("──────────────────────────")
        sb.appendLine("Subtotal: ${CurrencyUtils.formatCup(order.subtotalCup)}")
        if (order.servicePercent > 0) {
            val serviceVal = order.subtotalCup * (order.servicePercent / 100.0)
            sb.appendLine("Servicio (${order.servicePercent.toInt()}%): ${CurrencyUtils.formatCup(serviceVal)}")
        }
        if (order.discountPercent > 0) {
            val discVal = order.subtotalCup * (order.discountPercent / 100.0)
            sb.appendLine("Descuento (${order.discountPercent.toInt()}%): -${CurrencyUtils.formatCup(discVal)}")
        }
        sb.appendLine("TOTAL: ${CurrencyUtils.formatCup(order.totalCup)}")
        sb.appendLine("──────────────────────────")
        sb.appendLine("💵 EQUIVALENCIAS MULTIMONEDA:")
        sb.appendLine("• USD: ${CurrencyUtils.formatForeign(usdEquiv, "$")} (Tasa: $usdRate)")
        sb.appendLine("• MLC: ${CurrencyUtils.formatForeign(mlcEquiv, "MLC")} (Tasa: $mlcRate)")
        sb.appendLine("• EUR: ${CurrencyUtils.formatForeign(eurEquiv, "€")} (Tasa: $eurRate)")
        sb.appendLine("══════════════════════════")
        sb.appendLine("¡Gracias por su visita a Cienfuegos!")
        return sb.toString()
    }

    fun generateDailyReportText(
        closedOrders: List<OrderWithItems>,
        expenses: List<Expense>,
        rates: List<ExchangeRate>
    ): String {
        val totalSalesCup = closedOrders.sumOf { it.order.totalCup }
        val totalPaidUsd = closedOrders.sumOf { it.order.paidUsd }
        val totalPaidMlc = closedOrders.sumOf { it.order.paidMlc }
        val totalExpensesCup = expenses.sumOf { it.amountCup }
        val netCup = totalSalesCup - totalExpensesCup

        val usdRate = CurrencyUtils.getRateFor("USD", rates)

        val sb = StringBuilder()
        sb.appendLine("📊 REPORTE DE CIERRE - PALADAR LACHY")
        sb.appendLine("📍 Cienfuegos, Cuba")
        sb.appendLine("📅 Fecha: ${dateOnlyFormat.format(Date())}")
        sb.appendLine("══════════════════════════")
        sb.appendLine("💰 VENTAS REGISTRADAS:")
        sb.appendLine("• Cuentas cerradas: ${closedOrders.size}")
        sb.appendLine("• Total en CUP: ${CurrencyUtils.formatCup(totalSalesCup)}")
        if (totalPaidUsd > 0) {
            sb.appendLine("• Recaudado USD en efectivo: $ ${String.format(Locale.US, "%.2f", totalPaidUsd)}")
        }
        if (totalPaidMlc > 0) {
            sb.appendLine("• Recaudado MLC (Transfermóvil): MLC ${String.format(Locale.US, "%.2f", totalPaidMlc)}")
        }

        sb.appendLine("──────────────────────────")
        sb.appendLine("📉 GASTOS DEL DÍA:")
        sb.appendLine("• Total gastos: ${CurrencyUtils.formatCup(totalExpensesCup)}")
        expenses.forEach { exp ->
            sb.appendLine("  - ${exp.description} (${exp.category}): ${CurrencyUtils.formatCup(exp.amountCup)}")
        }

        sb.appendLine("──────────────────────────")
        sb.appendLine("💵 BALANCE NETO DEL DÍA:")
        sb.appendLine("• Ganancia Neta: ${CurrencyUtils.formatCup(netCup)}")
        sb.appendLine("• Equiv. en USD aprox: $ ${String.format(Locale.US, "%.2f", netCup / usdRate)}")
        sb.appendLine("══════════════════════════")
        sb.appendLine("Generado con Paladar Lachy (Offline)")
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
