package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExchangeRate
import com.example.data.model.MenuItem
import com.example.ui.utils.CurrencyUtils

@Composable
fun InventoryScreen(
    menuItems: List<MenuItem>,
    exchangeRates: List<ExchangeRate>,
    onAdjustStock: (itemId: Long, delta: Int) -> Unit,
    onSaveMenuItem: (MenuItem) -> Unit,
    onDeleteMenuItem: (MenuItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val usdRate = CurrencyUtils.getRateFor("USD", exchangeRates)

    val outOfStockCount = menuItems.count { it.trackStock && it.stock <= 0 }
    val lowStockCount = menuItems.count { it.trackStock && it.stock in 1..4 }

    val filterOptions = listOf("Todos", "Bajo Stock (< 5)", "Agotados", "Bebidas", "Platos Fuertes", "Entrantes", "Coctelería")

    val filteredList = menuItems.filter { item ->
        val matchesFilter = when (selectedFilter) {
            "Todos" -> true
            "Bajo Stock (< 5)" -> item.trackStock && item.stock in 1..4
            "Agotados" -> item.trackStock && item.stock <= 0
            else -> item.category == selectedFilter
        }
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header summary for Cuba stock alert
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
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
                            text = "Control de Existencias e Insumos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestiona platos y bebidas para no vender productos agotados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (outOfStockCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$outOfStockCount Agotados",
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .testTag("inventory_search_input"),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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

            // Inventory List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        usdRate = usdRate,
                        onAdjustStock = { delta -> onAdjustStock(item.id, delta) },
                        onEdit = { itemToEdit = item }
                    )
                }
            }
        }

        // Add Product FAB
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_product_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
        }
    }

    // Edit Item Dialog
    itemToEdit?.let { item ->
        EditMenuItemDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onSave = { updated ->
                onSaveMenuItem(updated)
                itemToEdit = null
            },
            onDelete = {
                onDeleteMenuItem(item)
                itemToEdit = null
            }
        )
    }

    // Create New Item Dialog
    if (showCreateDialog) {
        EditMenuItemDialog(
            item = MenuItem(
                name = "",
                category = "Platos Fuertes",
                priceCup = 1000.0,
                stock = 10,
                trackStock = true
            ),
            isNew = true,
            onDismiss = { showCreateDialog = false },
            onSave = { newItem ->
                onSaveMenuItem(newItem)
                showCreateDialog = false
            },
            onDelete = {}
        )
    }
}

@Composable
fun InventoryItemCard(
    item: MenuItem,
    usdRate: Double,
    onAdjustStock: (Int) -> Unit,
    onEdit: () -> Unit
) {
    val isOutOfStock = item.trackStock && item.stock <= 0
    val isLowStock = item.trackStock && item.stock in 1..4

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = item.category,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = CurrencyUtils.formatCup(item.priceCup),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "≈ $ ${String.format(java.util.Locale.US, "%.2f", item.priceCup / usdRate)} USD",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar ${item.name}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stock control row
            if (item.trackStock) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stockColor = when {
                        isOutOfStock -> Color(0xFFEF4444)
                        isLowStock -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    }
                    val stockText = when {
                        isOutOfStock -> "AGOTADO"
                        isLowStock -> "BAJO STOCK (${item.stock})"
                        else -> "En inventario: ${item.stock}"
                    }

                    Text(
                        text = stockText,
                        color = stockColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    // Quick adjustments (+1, +5, -1)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAdjustStock(-1) },
                            modifier = Modifier.size(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onAdjustStock(1) },
                            modifier = Modifier.size(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onAdjustStock(5) },
                            modifier = Modifier.size(36.dp, 32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditMenuItemDialog(
    item: MenuItem,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var priceText by remember { mutableStateOf(item.priceCup.toInt().toString()) }
    var stockText by remember { mutableStateOf(item.stock.toString()) }
    var trackStock by remember { mutableStateOf(item.trackStock) }
    var description by remember { mutableStateOf(item.description) }

    val presetCategories = listOf("Platos Fuertes", "Entrantes", "Guarniciones", "Bebidas", "Coctelería", "Postres", "Otros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "Nuevo Producto / Plato" else "Editar: ${item.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del plato o bebida") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoría:", style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(presetCategories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Precio (CUP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    if (trackStock) {
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("Stock Actual") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Controlar Stock:", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = trackStock, onCheckedChange = { trackStock = it })
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción o ingredientes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: item.priceCup
                    val stock = stockText.toIntOrNull() ?: item.stock
                    onSave(
                        item.copy(
                            name = name.trim(),
                            category = category,
                            priceCup = price,
                            stock = stock,
                            trackStock = trackStock,
                            description = description.trim()
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isNew) "Crear Producto" else "Guardar Cambios")
            }
        },
        dismissButton = {
            Row {
                if (!isNew) {
                    TextButton(
                        onClick = onDelete,
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}
