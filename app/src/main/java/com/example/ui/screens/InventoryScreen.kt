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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.utils.CurrencyUtils

@Composable
fun InventoryScreen(
    stockItems: List<StockItem>,
    onAdjustStock: (itemId: Long, newStock: Double, responsible: String, reason: String) -> Unit,
    onRegisterOutput: (itemId: Long, quantity: Double, responsible: String, reason: MovementType, notes: String) -> Unit,
    onRegisterDirectEntry: (itemId: Long, quantity: Double, unitPriceCup: Double, buyer: String, place: String, notes: String) -> Unit,
    onSaveItem: (StockItem) -> Unit,
    onDeleteItem: (StockItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    var itemToEdit by remember { mutableStateOf<StockItem?>(null) }
    var itemToQuickAdjust by remember { mutableStateOf<StockItem?>(null) }
    var itemForQuickOutput by remember { mutableStateOf<StockItem?>(null) }
    var itemForQuickEntry by remember { mutableStateOf<StockItem?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val categories = listOf("Todos", "Insumos", "Empaques", "Equipos y Utensilios", "Bebidas", "General")

    val filteredList = stockItems.filter { item ->
        val matchesCategory = selectedCategory == "Todos" || item.category == selectedCategory
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                item.notes.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
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
                        text = "Inventario & Almacén",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Control de existencias y catálogo de productos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar producto en almacén...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventory_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Products List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { "inv_item_${it.id}" }) { item ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Top Row: Name and Current Stock
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(item.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (badgeBg, badgeColor, badgeText) = when {
                                        item.isOutOfStock -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "AGOTADO")
                                        item.isCritical -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "STOCK BAJO")
                                        else -> Triple(Color(0xFFD1FAE5), Color(0xFF047857), "EN STOCK")
                                    }

                                    Surface(shape = RoundedCornerShape(6.dp), color = badgeBg) {
                                        Text(
                                            text = "${item.currentStock} ${item.unit}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(onClick = { itemToEdit = item }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Details: Price & Value
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (item.lastUnitPriceCup > 0) "Precio ref: ${CurrencyUtils.formatCup(item.lastUnitPriceCup)}/${item.unit}" else "Sin precio",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "Valor: ${CurrencyUtils.formatCup(item.totalValueCup)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: Entrada rápida, Salida rápida, Ajuste
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { itemForQuickEntry = item },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Entrada", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { itemForQuickOutput = item },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salida", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { itemToQuickAdjust = item },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Conteo", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Product FAB
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("inventory_add_item_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
        }
    }

    // Quick Entry Dialog
    itemForQuickEntry?.let { item ->
        var quantityText by remember { mutableStateOf("10") }
        var unitPriceText by remember { mutableStateOf(if (item.lastUnitPriceCup > 0) item.lastUnitPriceCup.toInt().toString() else "") }
        var placeText by remember { mutableStateOf("Mercado / Proveedor") }
        var buyerText by remember { mutableStateOf("Comprador") }
        var notesText by remember { mutableStateOf("") }

        val q = quantityText.toDoubleOrNull() ?: 0.0
        val p = unitPriceText.toDoubleOrNull() ?: item.lastUnitPriceCup

        AlertDialog(
            onDismissRequest = { itemForQuickEntry = null },
            title = { Text("Registrar Entrada a Almacén", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Producto: ${item.name}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Stock actual: ${item.currentStock} ${item.unit}", fontSize = 12.sp)

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Cantidad a ingresar (${item.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it },
                        label = { Text("Precio por ${item.unit} (CUP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Costo Total: ${CurrencyUtils.formatCup(q * p)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = buyerText,
                        onValueChange = { buyerText = it },
                        label = { Text("Comprador / Responsable") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = placeText,
                        onValueChange = { placeText = it },
                        label = { Text("Lugar / Proveedor") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (q > 0) {
                            onRegisterDirectEntry(item.id, q, p, buyerText, placeText, notesText)
                            itemForQuickEntry = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                ) {
                    Text("Ingresar al Almacén")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemForQuickEntry = null }) { Text("Cancelar") }
            }
        )
    }

    // Quick Output Dialog
    itemForQuickOutput?.let { item ->
        var quantityText by remember { mutableStateOf("1") }
        var responsible by remember { mutableStateOf("Almacenero") }
        var reason by remember { mutableStateOf(MovementType.SALIDA_DESPACHO) }
        var notes by remember { mutableStateOf("") }

        val q = quantityText.toDoubleOrNull() ?: 0.0

        AlertDialog(
            onDismissRequest = { itemForQuickOutput = null },
            title = { Text("Registrar Salida de Almacén", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Producto: ${item.name}", fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                    Text("Disponible actualmente: ${item.currentStock} ${item.unit}", fontSize = 12.sp)

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Cantidad a despachar (${item.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Motivo de salida:", fontSize = 12.sp)
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
                        label = { Text("Nota de salida") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (q > 0) {
                            onRegisterOutput(item.id, q, responsible, reason, notes)
                            itemForQuickOutput = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Descontar de Almacén")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemForQuickOutput = null }) { Text("Cancelar") }
            }
        )
    }

    // Quick Physical Count Adjustment
    itemToQuickAdjust?.let { item ->
        var newStockText by remember { mutableStateOf(item.currentStock.toString()) }
        var reason by remember { mutableStateOf("Conteo de inventario físico") }
        var responsible by remember { mutableStateOf("Almacenero") }

        AlertDialog(
            onDismissRequest = { itemToQuickAdjust = null },
            title = { Text("Ajustar Conteo Físico", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Producto: ${item.name} (${item.unit})", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = newStockText,
                        onValueChange = { newStockText = it },
                        label = { Text("Conteo Real Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo del ajuste") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val s = newStockText.toDoubleOrNull()
                    if (s != null) {
                        onAdjustStock(item.id, s, responsible, reason)
                        itemToQuickAdjust = null
                    }
                }) {
                    Text("Guardar Conteo")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToQuickAdjust = null }) { Text("Cancelar") }
            }
        )
    }

    // Edit Item Dialog
    itemToEdit?.let { item ->
        EditStockItemDialog(
            item = item,
            isNew = false,
            onDismiss = { itemToEdit = null },
            onSave = { updated ->
                onSaveItem(updated)
                itemToEdit = null
            },
            onDelete = {
                onDeleteItem(item)
                itemToEdit = null
            }
        )
    }

    // Create New Item Dialog
    if (showCreateDialog) {
        EditStockItemDialog(
            item = StockItem(
                name = "",
                category = "General",
                unit = "Unidades",
                currentStock = 0.0,
                minStockAlert = 5.0
            ),
            isNew = true,
            onDismiss = { showCreateDialog = false },
            onSave = { newItem ->
                onSaveItem(newItem)
                showCreateDialog = false
            },
            onDelete = {}
        )
    }
}

@Composable
fun EditStockItemDialog(
    item: StockItem,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (StockItem) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var unit by remember { mutableStateOf(item.unit) }
    var currentStockText by remember { mutableStateOf(item.currentStock.toString()) }
    var minStockText by remember { mutableStateOf(item.minStockAlert.toString()) }
    var unitPriceText by remember { mutableStateOf(if (item.lastUnitPriceCup > 0) item.lastUnitPriceCup.toInt().toString() else "") }
    var notes by remember { mutableStateOf(item.notes) }

    val presetCategories = listOf("Insumos", "Empaques", "Equipos y Utensilios", "Bebidas", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nuevo Producto en Almacén" else "Editar: ${item.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
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
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidad (Libras/u/Cajas)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("Alerta Mínima") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentStockText,
                        onValueChange = { currentStockText = it },
                        label = { Text("Stock Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it },
                        label = { Text("Precio Ref (CUP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de ubicación o proveedor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = currentStockText.toDoubleOrNull() ?: item.currentStock
                    val m = minStockText.toDoubleOrNull() ?: item.minStockAlert
                    val p = unitPriceText.toDoubleOrNull() ?: item.lastUnitPriceCup
                    if (name.isNotBlank()) {
                        onSave(
                            item.copy(
                                name = name.trim(),
                                category = category,
                                unit = unit.trim(),
                                currentStock = s,
                                minStockAlert = m,
                                lastUnitPriceCup = p,
                                notes = notes.trim(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isNew) "Guardar en Almacén" else "Guardar Cambios")
            }
        },
        dismissButton = {
            Row {
                if (!isNew) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}
