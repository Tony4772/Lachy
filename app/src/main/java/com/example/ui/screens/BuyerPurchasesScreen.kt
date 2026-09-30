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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.Switch
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
import com.example.data.model.PurchaseRecord
import com.example.data.model.PurchaseStatus
import com.example.data.model.StockItem
import com.example.ui.utils.CurrencyUtils

@Composable
fun BuyerPurchasesScreen(
    purchasesEnCamino: List<PurchaseRecord>,
    purchasesFaltantes: List<PurchaseRecord>,
    criticalItems: List<StockItem>,
    allItems: List<StockItem>,
    allPurchases: List<PurchaseRecord>,
    onRegisterPurchase: (
        purchaseId: Long?,
        itemName: String,
        quantity: Double,
        unit: String,
        unitPriceCup: Double,
        buyerName: String,
        purchasePlace: String,
        directlyReceived: Boolean,
        notes: String
    ) -> Unit,
    onConfirmReceipt: (purchase: PurchaseRecord, receiverName: String) -> Unit
) {
    var showRegisterDialog by remember { mutableStateOf(false) }
    var selectedItemForQuickBuy by remember { mutableStateOf<StockItem?>(null) }
    var selectedPurchaseToBuy by remember { mutableStateOf<PurchaseRecord?>(null) }
    var purchaseToConfirm by remember { mutableStateOf<PurchaseRecord?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            text = "Módulo del Comprador",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestión de compras, precios y entradas al almacén",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gestor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Explanatory card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Aquí el comprador consulta los productos que están bajos en almacén y registra cada compra indicando el precio pagado por unidad o libra. El dueño ve inmediatamente lo que se compró.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Section: Compras en camino (esperando ingresar al almacén)
            if (purchasesEnCamino.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Compras en Camino al Almacén (${purchasesEnCamino.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(purchasesEnCamino, key = { "buyer_transit_${it.id}" }) { p ->
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
                                Text(
                                    text = "${p.quantity} ${p.unit} de ${p.itemName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDBEAFE)) {
                                    Text(
                                        "EN CAMINO",
                                        color = Color(0xFF1D4ED8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "A ${CurrencyUtils.formatCup(p.unitPriceCup)}/${p.unit} • Total: ${CurrencyUtils.formatCup(p.totalCostCup)}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Lugar: ${p.purchasePlace} • Comprador: ${p.buyerName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { purchaseToConfirm = p },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirmar Ingreso a Almacén", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Section: Productos que hacen falta comprar
            item {
                Text(
                    text = "⚠️ Productos con Stock Bajo / Por Comprar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (criticalItems.isEmpty() && purchasesFaltantes.isEmpty()) {
                item {
                    Text(
                        text = "No hay solicitudes pendientes. El almacén tiene stock suficiente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(criticalItems, key = { "buyer_crit_${it.id}" }) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "Quedan: ${item.currentStock} ${item.unit} (Mínimo: ${item.minStockAlert})",
                                    fontSize = 12.sp,
                                    color = Color(0xFFDC2626)
                                )
                                if (item.lastUnitPriceCup > 0) {
                                    Text(
                                        "Último precio: ${CurrencyUtils.formatCup(item.lastUnitPriceCup)}/${item.unit}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    selectedItemForQuickBuy = item
                                    showRegisterDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Comprar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Section: Historial de Compras Recientes
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📦 Compras Registradas Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(allPurchases.take(12), key = { "buyer_all_${it.id}" }) { p ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${p.quantity} ${p.unit} de ${p.itemName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "${CurrencyUtils.formatCup(p.unitPriceCup)}/${p.unit} • Total: ${CurrencyUtils.formatCup(p.totalCostCup)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Lugar: ${p.purchasePlace} • Comprador: ${p.buyerName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val (statusColor, statusBg, statusText) = when (p.status) {
                            PurchaseStatus.COMPRADO_EN_CAMINO -> Triple(Color(0xFF1D4ED8), Color(0xFFDBEAFE), "EN CAMINO")
                            PurchaseStatus.RECIBIDO_EN_ALMACEN -> Triple(Color(0xFF047857), Color(0xFFD1FAE5), "EN ALMACÉN")
                            PurchaseStatus.NECESITA_COMPRAR -> Triple(Color(0xFFDC2626), Color(0xFFFEE2E2), "SOLICITADO")
                        }

                        Surface(shape = RoundedCornerShape(4.dp), color = statusBg) {
                            Text(
                                text = statusText,
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // FAB to Register Purchase
        FloatingActionButton(
            onClick = {
                selectedItemForQuickBuy = null
                selectedPurchaseToBuy = null
                showRegisterDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("buyer_register_purchase_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Registrar Compra")
        }
    }

    // Register Purchase Dialog
    if (showRegisterDialog) {
        RegisterPurchaseDialog(
            preselectedItem = selectedItemForQuickBuy,
            preselectedPurchase = selectedPurchaseToBuy,
            availableItems = allItems,
            onDismiss = {
                showRegisterDialog = false
                selectedItemForQuickBuy = null
                selectedPurchaseToBuy = null
            },
            onConfirm = { purchaseId, itemName, qty, unit, unitPrice, buyer, place, directlyReceived, notes ->
                onRegisterPurchase(purchaseId, itemName, qty, unit, unitPrice, buyer, place, directlyReceived, notes)
                showRegisterDialog = false
                selectedItemForQuickBuy = null
                selectedPurchaseToBuy = null
            }
        )
    }

    // Confirm Receipt Dialog
    purchaseToConfirm?.let { purchase ->
        var receiverName by remember { mutableStateOf("Almacenero") }

        AlertDialog(
            onDismissRequest = { purchaseToConfirm = null },
            title = { Text("Confirmar Entrada al Almacén", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Ingresó el producto físicamente al almacén?")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("${purchase.quantity} ${purchase.unit} de ${purchase.itemName}", fontWeight = FontWeight.Bold)
                            Text("Comprado por: ${purchase.buyerName} en ${purchase.purchasePlace}", fontSize = 12.sp)
                        }
                    }
                    OutlinedTextField(
                        value = receiverName,
                        onValueChange = { receiverName = it },
                        label = { Text("Recibido por (Nombre)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Se sumará automáticamente a las existencias disponibles del almacén.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmReceipt(purchase, receiverName)
                        purchaseToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                ) {
                    Text("Confirmar Ingreso")
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToConfirm = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun RegisterPurchaseDialog(
    preselectedItem: StockItem?,
    preselectedPurchase: PurchaseRecord?,
    availableItems: List<StockItem>,
    onDismiss: () -> Unit,
    onConfirm: (
        purchaseId: Long?,
        itemName: String,
        quantity: Double,
        unit: String,
        unitPriceCup: Double,
        buyerName: String,
        purchasePlace: String,
        directlyReceived: Boolean,
        notes: String
    ) -> Unit
) {
    var itemName by remember {
        mutableStateOf(preselectedPurchase?.itemName ?: preselectedItem?.name ?: "")
    }
    var unit by remember {
        mutableStateOf(preselectedPurchase?.unit ?: preselectedItem?.unit ?: "Libras")
    }
    var quantityText by remember {
        mutableStateOf(preselectedPurchase?.quantity?.toString() ?: "10")
    }
    var unitPriceText by remember {
        val defaultPrice = preselectedItem?.lastUnitPriceCup?.toInt()?.toString() ?: ""
        mutableStateOf(defaultPrice)
    }
    var buyerName by remember { mutableStateOf("Comprador") }
    var purchasePlace by remember { mutableStateOf("Mercado / Proveedor") }
    var directlyReceived by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    val qty = quantityText.toDoubleOrNull() ?: 0.0
    val price = unitPriceText.toDoubleOrNull() ?: 0.0
    val totalCost = qty * price

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Registrar Compra / Entrada", fontWeight = FontWeight.Bold)
                Text(
                    "El dueño verá el precio pagado y el estado de la compra",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Nombre del Producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("buyer_item_name_input")
                )

                if (itemName.isBlank() && availableItems.isNotEmpty()) {
                    Text("Selecciona de inventario existente:", style = MaterialTheme.typography.bodySmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(availableItems.take(6)) { item ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    itemName = item.name
                                    unit = item.unit
                                    if (item.lastUnitPriceCup > 0) {
                                        unitPriceText = item.lastUnitPriceCup.toInt().toString()
                                    }
                                },
                                label = { Text(item.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidad") },
                        placeholder = { Text("Libras/u") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it },
                    label = { Text("Precio por $unit (CUP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("buyer_unit_price_input")
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Costo Total:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            CurrencyUtils.formatCup(totalCost),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = purchasePlace,
                    onValueChange = { purchasePlace = it },
                    label = { Text("Lugar / Proveedor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Directly received switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¿Ingresó ya al almacén?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            if (directlyReceived) "Se sumará inmediatamente al stock" else "Quedará marcado 'En Camino'",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = directlyReceived,
                        onCheckedChange = { directlyReceived = it }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isNotBlank() && qty > 0) {
                        onConfirm(
                            preselectedPurchase?.id,
                            itemName.trim(),
                            qty,
                            unit.trim(),
                            price,
                            buyerName.trim(),
                            purchasePlace.trim(),
                            directlyReceived,
                            notes.trim()
                        )
                    }
                },
                enabled = itemName.isNotBlank() && qty > 0,
                modifier = Modifier.testTag("confirm_register_purchase_btn")
            ) {
                Text(if (directlyReceived) "Guardar e Ingresar" else "Guardar (En Camino)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
