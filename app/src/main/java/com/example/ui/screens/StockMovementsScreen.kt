package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.MovementType
import com.example.data.model.StockItem
import com.example.data.model.StockMovement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockMovementsScreen(
    movements: List<StockMovement>,
    stockItems: List<StockItem>,
    onRegisterOutput: (itemId: Long, quantity: Double, responsible: String, reason: MovementType, notes: String) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showOutputDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("Todos", "Entradas (+)", "Salidas (-)", "Ajustes")

    val filteredMovements = movements.filter { m ->
        when (selectedFilter) {
            "Entradas (+)" -> m.quantity > 0
            "Salidas (-)" -> m.quantity < 0
            "Ajustes" -> m.type == MovementType.AJUSTE_MANUAL
            else -> true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Salidas & Kárdex",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Historial cronológico de entradas y salidas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showOutputDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salida", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Movements List
            if (filteredMovements.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay movimientos para este filtro.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMovements, key = { "kardex_mov_${it.id}" }) { m ->
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
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
                                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(6.dp).size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(m.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            "${m.type.label} • Por: ${m.registeredBy}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val dateStr = dateFormat.format(Date(m.timestamp))
                                        Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                        if (m.notes.isNotBlank()) {
                                            Text(m.notes, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }

                                Text(
                                    text = if (m.quantity > 0) "+${m.quantity} ${m.unit}" else "${m.quantity} ${m.unit}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = color,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Register Output Dialog
    if (showOutputDialog) {
        RegisterOutputDialog(
            stockItems = stockItems,
            onDismiss = { showOutputDialog = false },
            onConfirm = { itemId, qty, responsible, reason, notes ->
                onRegisterOutput(itemId, qty, responsible, reason, notes)
                showOutputDialog = false
            }
        )
    }
}

@Composable
fun RegisterOutputDialog(
    stockItems: List<StockItem>,
    onDismiss: () -> Unit,
    onConfirm: (itemId: Long, quantity: Double, responsible: String, reason: MovementType, notes: String) -> Unit
) {
    var selectedItem by remember { mutableStateOf(stockItems.firstOrNull()) }
    var quantityText by remember { mutableStateOf("1") }
    var responsible by remember { mutableStateOf("Almacenero") }
    var reason by remember { mutableStateOf(MovementType.SALIDA_DESPACHO) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Salida de Almacén", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Selecciona el producto a retirar:", style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(stockItems) { item ->
                        FilterChip(
                            selected = selectedItem?.id == item.id,
                            onClick = { selectedItem = item },
                            label = { Text(item.name, fontSize = 11.sp) }
                        )
                    }
                }

                selectedItem?.let { item ->
                    Text(
                        "Disponible actualmente: ${item.currentStock} ${item.unit}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Cantidad a retirar (${item.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Motivo de la salida:", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = reason == MovementType.SALIDA_DESPACHO,
                        onClick = { reason = MovementType.SALIDA_DESPACHO },
                        label = { Text("Despacho", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = reason == MovementType.SALIDA_CONSUMO,
                        onClick = { reason = MovementType.SALIDA_CONSUMO },
                        label = { Text("Consumo", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = reason == MovementType.SALIDA_MERMA,
                        onClick = { reason = MovementType.SALIDA_MERMA },
                        label = { Text("Merma", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = responsible,
                    onValueChange = { responsible = it },
                    label = { Text("Entregado a / Responsable") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Nota (ej. Entrega para producción o venta)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val item = selectedItem
                    val qty = quantityText.toDoubleOrNull() ?: 0.0
                    if (item != null && qty > 0) {
                        onConfirm(item.id, qty, responsible, reason, notes)
                    }
                },
                enabled = selectedItem != null && (quantityText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Descontar de Almacén")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
