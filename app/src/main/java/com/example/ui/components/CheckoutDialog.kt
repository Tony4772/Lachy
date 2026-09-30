package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExchangeRate
import com.example.data.model.OrderWithItems
import com.example.ui.utils.CurrencyUtils
import java.util.Locale

@Composable
fun CheckoutDialog(
    orderWithItems: OrderWithItems,
    exchangeRates: List<ExchangeRate>,
    onDismiss: () -> Unit,
    onConfirmCheckout: (
        paidCup: Double,
        paidUsd: Double,
        paidMlc: Double,
        paidEur: Double,
        paymentMethod: String,
        changeCup: Double,
        notes: String
    ) -> Unit
) {
    val order = orderWithItems.order
    val usdRate = CurrencyUtils.getRateFor("USD", exchangeRates)
    val mlcRate = CurrencyUtils.getRateFor("MLC", exchangeRates)
    val eurRate = CurrencyUtils.getRateFor("EUR", exchangeRates)

    var serviceEnabled by remember { mutableStateOf(order.servicePercent > 0) }
    val servicePercent = if (serviceEnabled) 10.0 else 0.0

    val subtotal = order.subtotalCup
    val serviceAmount = subtotal * (servicePercent / 100.0)
    val totalCup = subtotal + serviceAmount

    val totalUsd = CurrencyUtils.cupToForeign(totalCup, usdRate)
    val totalMlc = CurrencyUtils.cupToForeign(totalCup, mlcRate)
    val totalEur = CurrencyUtils.cupToForeign(totalCup, eurRate)

    // Payment amounts input
    var paidCupText by remember { mutableStateOf("") }
    var paidUsdText by remember { mutableStateOf("") }
    var paidMlcText by remember { mutableStateOf("") }
    var paidEurText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val paidCupVal = paidCupText.toDoubleOrNull() ?: 0.0
    val paidUsdVal = paidUsdText.toDoubleOrNull() ?: 0.0
    val paidMlcVal = paidMlcText.toDoubleOrNull() ?: 0.0
    val paidEurVal = paidEurText.toDoubleOrNull() ?: 0.0

    // Convert all paid foreign currencies into CUP equivalent
    val totalPaidInCup = paidCupVal +
            (paidUsdVal * usdRate) +
            (paidMlcVal * mlcRate) +
            (paidEurVal * eurRate)

    val differenceCup = totalPaidInCup - totalCup
    val isExactOrMore = totalPaidInCup >= totalCup - 0.5 // small rounding tolerance

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Cobro Multimoneda (Cuba)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mesa: ${order.tableName} • ${orderWithItems.itemsCount} productos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Total Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total a Pagar:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = CurrencyUtils.formatCup(totalCup),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Equivalences chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "USD: $ ${String.format(Locale.US, "%.2f", totalUsd)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "MLC: ${String.format(Locale.US, "%.2f", totalMlc)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "EUR: € ${String.format(Locale.US, "%.2f", totalEur)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Service 10% toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Servicio de Mesa (10%)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tradicional en Cuba: +${CurrencyUtils.formatCup(serviceAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = serviceEnabled,
                        onCheckedChange = { serviceEnabled = it },
                        modifier = Modifier.testTag("service_charge_switch")
                    )
                }

                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

                Text(
                    text = "Desglose del Pago (Efectivo / Transferencia)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                // Quick full payment in CUP button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            paidCupText = totalCup.toInt().toString()
                            paidUsdText = ""
                            paidMlcText = ""
                            paidEurText = ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Todo en CUP", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            paidCupText = ""
                            paidUsdText = String.format(Locale.US, "%.2f", totalUsd)
                            paidMlcText = ""
                            paidEurText = ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Todo en USD", fontSize = 12.sp)
                    }
                }

                // Currency input fields
                OutlinedTextField(
                    value = paidCupText,
                    onValueChange = { paidCupText = it },
                    label = { Text("Efectivo CUP") },
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paid_cup_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = paidUsdText,
                        onValueChange = { paidUsdText = it },
                        label = { Text("USD ($1 = $usdRate CUP)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("paid_usd_input")
                    )

                    OutlinedTextField(
                        value = paidMlcText,
                        onValueChange = { paidMlcText = it },
                        label = { Text("MLC ($1 = $mlcRate CUP)") },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("paid_mlc_input")
                    )
                }

                OutlinedTextField(
                    value = paidEurText,
                    onValueChange = { paidEurText = it },
                    label = { Text("Euros (€1 = $eurRate CUP)") },
                    leadingIcon = { Icon(Icons.Default.Euro, contentDescription = null) },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Change or pending summary
                val statusBg = if (differenceCup >= 0) Color(0xFF10B981) else Color(0xFFE11D48)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusBg.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (differenceCup >= 0) "Vuelto a entregar (CUP):" else "Falta por pagar:",
                            fontWeight = FontWeight.Bold,
                            color = statusBg
                        )
                        Text(
                            text = CurrencyUtils.formatCup(Math.abs(differenceCup)),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = statusBg
                        )
                    }
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Nota de cobro (Opcional)") },
                    placeholder = { Text("Ej: Pago con billete de 100 USD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val method = when {
                        paidUsdVal > 0 && paidCupVal > 0 -> "Mixto (USD + CUP)"
                        paidMlcVal > 0 && paidCupVal > 0 -> "Mixto (MLC + CUP)"
                        paidUsdVal > 0 -> "Efectivo USD"
                        paidMlcVal > 0 -> "Transferencia MLC (Transfermóvil)"
                        paidEurVal > 0 -> "Efectivo EUR"
                        else -> "Efectivo CUP"
                    }
                    val change = if (differenceCup > 0) differenceCup else 0.0
                    onConfirmCheckout(
                        paidCupVal,
                        paidUsdVal,
                        paidMlcVal,
                        paidEurVal,
                        method,
                        change,
                        notesText
                    )
                },
                enabled = isExactOrMore,
                modifier = Modifier.testTag("confirm_checkout_btn")
            ) {
                Text("Confirmar y Cerrar Mesa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
