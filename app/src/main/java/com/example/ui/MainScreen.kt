package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ItemCookingStatus
import com.example.data.model.MenuItem
import com.example.data.model.OrderItem
import com.example.ui.screens.CashClosureScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.OrderPosScreen
import com.example.ui.screens.RatesScreen
import com.example.ui.screens.TablesOverviewScreen
import com.example.ui.utils.CurrencyUtils
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RestaurantViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val tables by viewModel.tables.collectAsStateWithLifecycle()
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    val exchangeRates by viewModel.exchangeRates.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val closedOrders by viewModel.closedOrders.collectAsStateWithLifecycle()
    val activeOrderWithItems by viewModel.activeOrderWithItems.collectAsStateWithLifecycle()
    val selectedTableId by viewModel.selectedTableId.collectAsStateWithLifecycle()

    val selectedTable = tables.find { it.id == selectedTableId }
    val usdRate = CurrencyUtils.getRateFor("USD", exchangeRates)

    // Back handling: if in POS or other secondary screen, back goes to Tables
    BackHandler(enabled = currentTab != AppTab.TABLES) {
        viewModel.setTab(AppTab.TABLES)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Paladar Lachy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.WifiOff,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFF10B981)
                            )
                            Text(
                                text = "100% Offline • 1 USD = ${usdRate.toInt()} CUP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                // Tables Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.TABLES,
                    onClick = { viewModel.setTab(AppTab.TABLES) },
                    icon = { Icon(Icons.Default.TableRestaurant, contentDescription = "Mesas") },
                    label = { Text("Mesas", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tables")
                )

                // POS / Comanda Tab
                val activeItemsCount = activeOrderWithItems?.itemsCount ?: 0
                NavigationBarItem(
                    selected = currentTab == AppTab.POS,
                    onClick = { viewModel.setTab(AppTab.POS) },
                    icon = {
                        if (activeItemsCount > 0) {
                            BadgedBox(badge = { Badge { Text("$activeItemsCount") } }) {
                                Icon(Icons.Default.RestaurantMenu, contentDescription = "Comanda")
                            }
                        } else {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = "Comanda")
                        }
                    },
                    label = { Text("Comanda", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_pos")
                )

                // Inventario Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.INVENTORY,
                    onClick = { viewModel.setTab(AppTab.INVENTORY) },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventario") },
                    label = { Text("Inventario", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_inventory")
                )

                // Caja / Cuadre Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.CASH,
                    onClick = { viewModel.setTab(AppTab.CASH) },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Caja") },
                    label = { Text("Caja", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_cash")
                )

                // Tasas de cambio Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.RATES,
                    onClick = { viewModel.setTab(AppTab.RATES) },
                    icon = { Icon(Icons.Default.CurrencyExchange, contentDescription = "Tasas") },
                    label = { Text("Tasas", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_rates")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.TABLES -> {
                    TablesOverviewScreen(
                        tables = tables,
                        exchangeRates = exchangeRates,
                        onSelectTable = { table -> viewModel.selectTable(table) },
                        onOpenTable = { tableId, waiter, diners ->
                            viewModel.openTable(tableId, waiter, diners) {
                                // Handled in ViewModel
                            }
                        },
                        onAddTable = { name, area, cap ->
                            viewModel.addTable(name, area, cap)
                        }
                    )
                }

                AppTab.POS -> {
                    OrderPosScreen(
                        table = selectedTable,
                        orderWithItems = activeOrderWithItems,
                        menuItems = menuItems,
                        exchangeRates = exchangeRates,
                        onBack = { viewModel.setTab(AppTab.TABLES) },
                        onAddItem = { item, qty, note ->
                            viewModel.addItemToActiveOrder(item, qty, note)
                        },
                        onRemoveItem = { item ->
                            viewModel.removeItemFromOrder(item)
                        },
                        onUpdateItemStatus = { itemId, status ->
                            viewModel.updateItemStatus(itemId, status)
                        },
                        onToggleService = { servicePercent ->
                            val orderId = viewModel.selectedOrderId.value ?: return@OrderPosScreen
                            viewModel.updateOrderServiceCharge(orderId, servicePercent)
                        },
                        onRequestBill = {
                            viewModel.requestBillForCurrentTable()
                        },
                        onCheckout = { paidCup, paidUsd, paidMlc, paidEur, method, changeCup, notes ->
                            viewModel.checkoutCurrentOrder(paidCup, paidUsd, paidMlc, paidEur, method, changeCup, notes)
                        },
                        onCancelOrder = {
                            viewModel.cancelActiveOrder()
                        }
                    )
                }

                AppTab.INVENTORY -> {
                    InventoryScreen(
                        menuItems = menuItems,
                        exchangeRates = exchangeRates,
                        onAdjustStock = { itemId, delta ->
                            viewModel.adjustStock(itemId, delta)
                        },
                        onSaveMenuItem = { item ->
                            viewModel.saveMenuItem(item)
                        },
                        onDeleteMenuItem = { item ->
                            viewModel.deleteMenuItem(item)
                        }
                    )
                }

                AppTab.CASH -> {
                    CashClosureScreen(
                        closedOrders = closedOrders,
                        expenses = expenses,
                        exchangeRates = exchangeRates,
                        onAddExpense = { desc, amount, category, notes ->
                            viewModel.addExpense(desc, amount, category, notes)
                        },
                        onDeleteExpense = { exp ->
                            viewModel.deleteExpense(exp)
                        }
                    )
                }

                AppTab.RATES -> {
                    RatesScreen(
                        exchangeRates = exchangeRates,
                        onUpdateRate = { code, rate ->
                            viewModel.updateExchangeRate(code, rate)
                        }
                    )
                }
            }
        }
    }
}
