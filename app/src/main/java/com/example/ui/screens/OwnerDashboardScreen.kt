package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExchangeRate
import com.example.data.model.MovementType
import com.example.data.model.PurchaseRecord
import com.example.data.model.StockItem
import com.example.data.model.StockMovement
import com.example.ui.utils.CurrencyUtils
import com.example.ui.utils.ShareReportHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OwnerDashboardScreen(
    stockItems: List<StockItem>,
    criticalItems: List<StockItem>,
    purchasesEnCamino: List<PurchaseRecord>,
    recentMovements: List<StockMovement>,
    exchangeRates: List<ExchangeRate>,
    onNavigateToInventory: () -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToMovements: () -> Unit
) {
    val context = LocalContext.current
    val usdRate = CurrencyUtils.getRateFor("USD", exchangeRates)
    val totalInventoryValueCup = stockItems.sumOf { it.totalValueCup }
    val totalInventoryValueUsd = if (usdRate > 0) totalInventoryValueCup / usdRate else 0.0

    val outOfStockCount = stockItems.count { it.isOutOfStock }
    val lowStockCount = stockItems.count { it.isCritical }

    val recentOutputs = recentMovements.filter { it.quantity < 0 }.take(4)
    val recentEntries = recentMovements.filter { it.quantity > 0 }.take(4)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Panel del Dueño",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Control total de almacén e inventario",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // WhatsApp share button
                Button(
                    onClick = {
                        val report = ShareReportHelper.generateLachyReport(
                            purchasesEnCamino = purchasesEnCamino,
                            criticalItems = criticalItems,
                            allItems = stockItems,
                            usdRate = usdRate
                        )
                        ShareReportHelper.shareText(
                            context,
                            "Reporte de Inventario - Lachy",
                            report
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("owner_share_whatsapp_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Main Metric Card: Total Inventory Value
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "VALOR TOTAL DEL INVENTARIO EN ALMACÉN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = CurrencyUtils.formatCup(totalInventoryValueCup),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "≈ $ ${String.format(Locale.US, "%.2f", totalInventoryValueUsd)} USD (1 USD = $usdRate CUP)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Productos:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            Text("${stockItems.size} ítems", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("En Camino:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            Text("${purchasesEnCamino.size} compras", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1D4ED8))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Stock Bajo/Crítico:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            Text("${criticalItems.size} productos", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (criticalItems.isNotEmpty()) Color(0xFFDC2626) else MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        // Section: Compras en camino (¿Qué compró el comprador y aún no llega al almacén?)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Compras en Camino al Almacén (${purchasesEnCamino.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onNavigateToPurchases) {
                    Text("Ver Compras", fontSize = 12.sp)
                }
            }
        }

        if (purchasesEnCamino.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "No hay compras en tránsito. Todo lo comprado ha ingresado al almacén.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(purchasesEnCamino, key = { "owner_dash_p_${it.id}" }) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${p.quantity} ${p.unit} de ${p.itemName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Comprador: ${p.buyerName} • ${p.purchasePlace}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${CurrencyUtils.formatCup(p.unitPriceCup)}/${p.unit}",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Total: ${CurrencyUtils.formatCup(p.totalCostCup)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: Alertas de Stock Bajo
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Alertas de Stock Bajo / Por Agotarse (${criticalItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onNavigateToInventory) {
                    Text("Ver Inventario", fontSize = 12.sp)
                }
            }
        }

        if (criticalItems.isEmpty()) {
            item {
                Text(
                    text = "Todos los productos del almacén se encuentran en nivel adecuado.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(criticalItems, key = { "owner_dash_crit_${it.id}" }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B), fontSize = 14.sp)
                            Text(
                                "Quedan: ${item.currentStock} ${item.unit} (Mínimo: ${item.minStockAlert})",
                                fontSize = 12.sp,
                                color = Color(0xFFB91C1C)
                            )
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDC2626)) {
                            Text(
                                text = if (item.isOutOfStock) "AGOTADO" else "STOCK BAJO",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Últimos Movimientos (Entradas y Salidas)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos Movimientos de Almacén",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToMovements) {
                    Text("Ver Kárdex", fontSize = 12.sp)
                }
            }
        }

        if (recentMovements.isEmpty()) {
            item {
                Text("No hay movimientos registrados en el almacén.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(recentMovements.take(5), key = { "owner_dash_mov_${it.id}" }) { m ->
                val isEntry = m.quantity > 0
                val icon = if (isEntry) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
                val color = if (isEntry) Color(0xFF047857) else Color(0xFFDC2626)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
                                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(m.itemName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${m.type.label} • Por: ${m.registeredBy}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(
                            text = if (m.quantity > 0) "+${m.quantity} ${m.unit}" else "${m.quantity} ${m.unit}",
                            fontWeight = FontWeight.Bold,
                            color = color,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
