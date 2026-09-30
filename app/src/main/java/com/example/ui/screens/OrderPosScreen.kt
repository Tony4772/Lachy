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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.ItemCookingStatus
import com.example.data.model.MenuItem
import com.example.data.model.OrderItem
import com.example.data.model.OrderWithItems
import com.example.data.model.TableEntity
import com.example.ui.components.CheckoutDialog
import com.example.ui.components.TicketDialog
import com.example.ui.utils.CurrencyUtils
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderPosScreen(
    table: TableEntity?,
    orderWithItems: OrderWithItems?,
    menuItems: List<MenuItem>,
    exchangeRates: List<ExchangeRate>,
    onBack: () -> Unit,
    onAddItem: (menuItem: MenuItem, quantity: Int, notes: String) -> Unit,
    onRemoveItem: (OrderItem) -> Unit,
    onUpdateItemStatus: (itemId: Long, status: ItemCookingStatus) -> Unit,
    onToggleService: (servicePercent: Double) -> Unit,
    onRequestBill: () -> Unit,
    onCheckout: (
        paidCup: Double,
        paidUsd: Double,
        paidMlc: Double,
        paidEur: Double,
        paymentMethod: String,
        changeCup: Double,
        notes: String
    ) -> Unit,
    onCancelOrder: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var itemToAddCustom by remember { mutableStateOf<MenuItem?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showTicketDialog by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var showOrderSheet by remember { mutableStateOf(false) }

    val usdRate = CurrencyUtils.getRateFor("USD", exchangeRates)

    val categories = listOf("Todos") + menuItems.map { it.category }.distinct()

    val filteredItems = menuItems.filter { item ->
        val matchesCategory = selectedCategory == "Todos" || item.category == selectedCategory
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    if (orderWithItems == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text("No hay comanda activa seleccionada", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onBack) {
                    Text("Volver a Mesas")
                }
            }
        }
        return
    }

    val order = orderWithItems.order
    val items = orderWithItems.items
    val totalCup = order.totalCup
    val totalUsd = CurrencyUtils.cupToForeign(totalCup, usdRate)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Top Bar with Table Info & Live Subtotal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("pos_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                    Column {
                        Text(
                            text = order.tableName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Salonero: ${order.waiterName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live total pill (clickable to view order sheet)
                Surface(
                    onClick = { showOrderSheet = true },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.testTag("live_order_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyUtils.formatCup(totalCup),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "≈ $ ${String.format(Locale.US, "%.2f", totalUsd)} USD",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "${orderWithItems.itemsCount}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Search text field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .testTag("pos_search_input"),
                placeholder = { Text("Buscar plato o bebida (ej. Cristal, Ropa Vieja)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 13.sp) }
                    )
                }
            }

            // Menu Items Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    MenuItemGridCard(
                        item = item,
                        usdRate = usdRate,
                        onQuickAdd = { onAddItem(item, 1, "") },
                        onCustomAdd = { itemToAddCustom = item }
                    )
                }
            }
        }

        // Persistent Bottom Action Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View order sheet button
                OutlinedButton(
                    onClick = { showOrderSheet = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver (${orderWithItems.itemsCount})")
                }

                // Checkout button
                Button(
                    onClick = { showCheckoutDialog = true },
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("checkout_main_btn"),
                    enabled = items.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cobrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal Bottom Sheet for Active Order Details & Cooking Status
    if (showOrderSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = { showOrderSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Comanda: ${order.tableName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${items.size} renglones • ${orderWithItems.itemsCount} porciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(onClick = { showTicketDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir ticket")
                        }
                        IconButton(onClick = { showCancelConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Cancelar orden", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Items list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (items.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aún no se han añadido platos a esta mesa")
                            }
                        }
                    }

                    items(items, key = { it.id }) { orderItem ->
                        OrderItemRow(
                            item = orderItem,
                            onRemove = { onRemoveItem(orderItem) },
                            onToggleStatus = {
                                val next = when (orderItem.status) {
                                    ItemCookingStatus.PENDING -> ItemCookingStatus.COOKING
                                    ItemCookingStatus.COOKING -> ItemCookingStatus.SERVED
                                    ItemCookingStatus.SERVED -> ItemCookingStatus.PENDING
                                }
                                onUpdateItemStatus(orderItem.id, next)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Service 10% toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Servicio (10% sugerido en Cuba)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            val nextService = if (order.servicePercent > 0) 0.0 else 10.0
                            onToggleService(nextService)
                        }
                    ) {
                        Text(if (order.servicePercent > 0) "10% Activo" else "Sin Servicio")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                    Text(CurrencyUtils.formatCup(order.subtotalCup), fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyUtils.formatCup(totalCup),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "≈ $ ${String.format(Locale.US, "%.2f", totalUsd)} USD",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onRequestBill()
                            scope.launch { sheetState.hide() }
                            showOrderSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pedir Cuenta")
                    }

                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }
                            showOrderSheet = false
                            showCheckoutDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        enabled = items.isNotEmpty()
                    ) {
                        Text("Cobrar")
                    }
                }
            }
        }
    }

    // Custom add dialog (quantity and kitchen note)
    itemToAddCustom?.let { item ->
        AddCustomItemDialog(
            item = item,
            onDismiss = { itemToAddCustom = null },
            onConfirm = { qty, note ->
                onAddItem(item, qty, note)
                itemToAddCustom = null
            }
        )
    }

    // Checkout dialog
    if (showCheckoutDialog) {
        CheckoutDialog(
            orderWithItems = orderWithItems,
            exchangeRates = exchangeRates,
            onDismiss = { showCheckoutDialog = false },
            onConfirmCheckout = { paidCup, paidUsd, paidMlc, paidEur, method, changeCup, notes ->
                showCheckoutDialog = false
                onCheckout(paidCup, paidUsd, paidMlc, paidEur, method, changeCup, notes)
            }
        )
    }

    // Ticket dialog
    if (showTicketDialog) {
        TicketDialog(
            orderWithItems = orderWithItems,
            exchangeRates = exchangeRates,
            onDismiss = { showTicketDialog = false }
        )
    }

    // Cancel order confirm
    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("¿Cancelar esta comanda?") },
            text = { Text("Se cancelará la orden de la ${order.tableName} y se devolverán las existencias al inventario.") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirm = false
                        onCancelOrder()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, Cancelar Comanda")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) { Text("Volver") }
            }
        )
    }
}

@Composable
fun MenuItemGridCard(
    item: MenuItem,
    usdRate: Double,
    onQuickAdd: () -> Unit,
    onCustomAdd: () -> Unit
) {
    val isOutOfStock = item.trackStock && item.stock <= 0
    val isLowStock = item.trackStock && item.stock in 1..4
    val equivUsd = CurrencyUtils.cupToForeign(item.priceCup, usdRate)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isOutOfStock) { onCustomAdd() }
            .testTag("menu_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isOutOfStock) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Category & Stock Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                if (item.trackStock) {
                    val stockColor = when {
                        isOutOfStock -> Color(0xFFEF4444)
                        isLowStock -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    }
                    val stockText = when {
                        isOutOfStock -> "Agotado"
                        isLowStock -> "Quedan ${item.stock}"
                        else -> "${item.stock} disp."
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = stockColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stockText,
                            color = stockColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price and Add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = CurrencyUtils.formatCup(item.priceCup),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "≈ $ ${String.format(Locale.US, "%.2f", equivUsd)} USD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onQuickAdd,
                    enabled = !isOutOfStock,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isOutOfStock) Color.Gray.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                        .testTag("add_btn_${item.id}")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Añadir ${item.name}",
                        modifier = Modifier.size(20.dp),
                        tint = if (isOutOfStock) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(
    item: OrderItem,
    onRemove: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
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
                    text = "${item.quantity}x ${item.name}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (item.notes.isNotBlank()) {
                    Text(
                        text = "Nota: ${item.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = CurrencyUtils.formatCup(item.totalCup),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status badge (clickable to change)
                val (statusColor, statusBg) = when (item.status) {
                    ItemCookingStatus.PENDING -> Pair(Color(0xFFD97706), Color(0xFFFEF3C7))
                    ItemCookingStatus.COOKING -> Pair(Color(0xFF2563EB), Color(0xFFDBEAFE))
                    ItemCookingStatus.SERVED -> Pair(Color(0xFF059669), Color(0xFFD1FAE5))
                }

                Surface(
                    onClick = onToggleStatus,
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = item.status.label,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar renglón",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCustomItemDialog(
    item: MenuItem,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, note: String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Añadir ${item.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Precio unitario: ${CurrencyUtils.formatCup(item.priceCup)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cantidad:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos")
                        }
                        Text(
                            text = "$quantity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { quantity++ }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más")
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota para cocina / barra (opcional)") },
                    placeholder = { Text("Ej: Término medio, con limón extra, para llevar") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Total: ${CurrencyUtils.formatCup(item.priceCup * quantity)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(quantity, note) }) {
                Text("Añadir a Comanda")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
