package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExchangeRate
import com.example.ui.utils.CurrencyUtils
import java.util.Locale

@Composable
fun RatesScreen(
    exchangeRates: List<ExchangeRate>,
    onUpdateRate: (code: String, rate: Double) -> Unit
) {
    val usdRate = CurrencyUtils.getRateFor("USD", exchangeRates)
    val mlcRate = CurrencyUtils.getRateFor("MLC", exchangeRates)
    val eurRate = CurrencyUtils.getRateFor("EUR", exchangeRates)

    var usdInput by remember(usdRate) { mutableStateOf(usdRate.toInt().toString()) }
    var mlcInput by remember(mlcRate) { mutableStateOf(mlcRate.toInt().toString()) }
    var eurInput by remember(eurRate) { mutableStateOf(eurRate.toInt().toString()) }

    var converterCupInput by remember { mutableStateOf("3000") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Info
        Text(
            text = "Tasas de Cambio en Cuba",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "En Cuba las tasas del mercado informal cambian con frecuencia. Actualiza aquí la tasa del día para que el restaurante calcule automáticamente los cobros y vueltos en USD, MLC y EUR.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        // Rates Configuration Cards
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Configuración de Monedas (vs 1 CUP)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                // USD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = usdInput,
                        onValueChange = { usdInput = it },
                        label = { Text("1 Dólar (USD) en CUP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rate_usd_input")
                    )
                    Button(
                        onClick = {
                            val r = usdInput.toDoubleOrNull()
                            if (r != null && r > 0) onUpdateRate("USD", r)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Fijar")
                    }
                }

                // MLC
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = mlcInput,
                        onValueChange = { mlcInput = it },
                        label = { Text("1 MLC (Transferencia) en CUP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rate_mlc_input")
                    )
                    Button(
                        onClick = {
                            val r = mlcInput.toDoubleOrNull()
                            if (r != null && r > 0) onUpdateRate("MLC", r)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Fijar")
                    }
                }

                // EUR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = eurInput,
                        onValueChange = { eurInput = it },
                        label = { Text("1 Euro (EUR) en CUP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val r = eurInput.toDoubleOrNull()
                            if (r != null && r > 0) onUpdateRate("EUR", r)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Fijar")
                    }
                }
            }
        }

        // Quick Conversor / Simulator
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calculadora Rápida para el Mesero",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedTextField(
                    value = converterCupInput,
                    onValueChange = { converterCupInput = it },
                    label = { Text("Monto de cuenta en CUP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                val testCup = converterCupInput.toDoubleOrNull() ?: 0.0

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Equivalentes a cobrar al cliente:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("En Dólares (USD):", fontSize = 14.sp)
                            Text(
                                "$ ${String.format(Locale.US, "%.2f", testCup / usdRate)} USD",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("En MLC (Transfermóvil):", fontSize = 14.sp)
                            Text(
                                "MLC ${String.format(Locale.US, "%.2f", testCup / mlcRate)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("En Euros (EUR):", fontSize = 14.sp)
                            Text(
                                "€ ${String.format(Locale.US, "%.2f", testCup / eurRate)} EUR",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
