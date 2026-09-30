package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExchangeRate
import com.example.data.model.TableEntity
import com.example.data.model.TableStatus
import com.example.ui.utils.CurrencyUtils

@Composable
fun TablesOverviewScreen(
    tables: List<TableEntity>,
    exchangeRates: List<ExchangeRate>,
    onSelectTable: (TableEntity) -> Unit,
    onOpenTable: (tableId: Long, waiter: String, diners: Int) -> Unit,
    onAddTable: (name: String, area: String, capacity: Int) -> Unit
) {
    var selectedArea by remember { mutableStateOf("Todas") }
    var tableToOpen by remember { mutableStateOf<TableEntity?>(null) }
    var showAddTableDialog by remember { mutableStateOf(false) }

    val areas = listOf("Todas") + tables.map { it.area }.distinct()
    val filteredTables = if (selectedArea == "Todas") {
        tables
    } else {
        tables.filter { it.area == selectedArea }
    }

    val occupiedCount = tables.count { it.status != TableStatus.FREE }
    val freeCount = tables.count { it.status == TableStatus.FREE }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Stats banner for quick glance in the restaurant
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Paladar Lachy • Cienfuegos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Sistema 100% Offline (Sin conexión)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text(
                                text = "$freeCount Libres",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE11D48).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48))
                        ) {
                            Text(
                                text = "$occupiedCount Ocupadas",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color(0xFFBE123C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Area filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(areas) { area ->
                    FilterChip(
                        selected = selectedArea == area,
                        onClick = { selectedArea = area },
                        label = { Text(area) },
                        modifier = Modifier.testTag("filter_chip_$area")
                    )
                }
            }

            // Tables Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTables, key = { it.id }) { table ->
                    TableGridItem(
                        table = table,
                        onClick = {
                            if (table.status == TableStatus.FREE) {
                                tableToOpen = table
                            } else {
                                onSelectTable(table)
                            }
                        }
                    )
                }
            }
        }

        // Floating button to add custom table
        FloatingActionButton(
            onClick = { showAddTableDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_table_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Mesa")
        }
    }

    // Dialog to open table
    tableToOpen?.let { table ->
        OpenTableDialog(
            table = table,
            onDismiss = { tableToOpen = null },
            onConfirm = { waiter, diners ->
                onOpenTable(table.id, waiter, diners)
                tableToOpen = null
            }
        )
    }

    // Dialog to create new table
    if (showAddTableDialog) {
        AddTableDialog(
            onDismiss = { showAddTableDialog = false },
            onConfirm = { name, area, cap ->
                onAddTable(name, area, cap)
                showAddTableDialog = false
            }
        )
    }
}

@Composable
fun TableGridItem(
    table: TableEntity,
    onClick: () -> Unit
) {
    val isOccupied = table.status != TableStatus.FREE
    val isBillRequested = table.status == TableStatus.BILL_REQUESTED

    val statusColor = when (table.status) {
        TableStatus.FREE -> Color(0xFF10B981) // Emerald Green
        TableStatus.OCCUPIED -> Color(0xFF2563EB) // Royal Blue
        TableStatus.BILL_REQUESTED -> Color(0xFFD97706) // Amber/Gold
    }

    val cardBg = if (isOccupied) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table_card_${table.id}"),
        colors = CardDefaults.elevatedCardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
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
                    text = table.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Text(
                text = table.area,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status label & details
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = when (table.status) {
                        TableStatus.FREE -> "Libre"
                        TableStatus.OCCUPIED -> "Ocupada"
                        TableStatus.BILL_REQUESTED -> "Pidiendo Cuenta"
                    }
                    val statusIcon = when (table.status) {
                        TableStatus.FREE -> Icons.Default.CheckCircle
                        TableStatus.OCCUPIED -> Icons.Default.Restaurant
                        TableStatus.BILL_REQUESTED -> Icons.Default.Receipt
                    }

                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isOccupied) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${table.dinersCount} pers. • ${table.currentWaiter}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ver comanda",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TableRestaurant,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cap: ${table.capacity} personas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OpenTableDialog(
    table: TableEntity,
    onDismiss: () -> Unit,
    onConfirm: (waiter: String, diners: Int) -> Unit
) {
    var waiter by remember { mutableStateOf("Mesero") }
    var diners by remember { mutableIntStateOf(table.capacity.coerceAtLeast(2)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Abrir ${table.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Área: ${table.area} • Iniciar comanda",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = waiter,
                    onValueChange = { waiter = it },
                    label = { Text("Nombre del Salonero / Mesero") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("waiter_input")
                )

                Text(
                    text = "Número de personas: $diners",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 4, 6, 8).forEach { count ->
                        OutlinedButton(
                            onClick = { diners = count },
                            colors = if (diners == count) ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "$count")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(waiter, diners) },
                modifier = Modifier.testTag("confirm_open_table_btn")
            ) {
                Text("Iniciar Comanda")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddTableDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, area: String, capacity: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("Salón Principal") }
    var capacity by remember { mutableIntStateOf(4) }

    val presetAreas = listOf("Salón Principal", "Terraza", "Barra", "Reservado", "Para Llevar")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Mesa / Área", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Mesa 5, Terraza 3)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Área:", style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetAreas) { preset ->
                        FilterChip(
                            selected = area == preset,
                            onClick = { area = preset },
                            label = { Text(preset) }
                        )
                    }
                }

                OutlinedTextField(
                    value = capacity.toString(),
                    onValueChange = { capacity = it.toIntOrNull() ?: 2 },
                    label = { Text("Capacidad de comensales") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, area, capacity)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar Mesa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
