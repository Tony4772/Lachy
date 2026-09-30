package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MovementType
import com.example.ui.screens.BuyerPurchasesScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.OwnerDashboardScreen
import com.example.ui.screens.StockMovementsScreen
import com.example.ui.utils.CurrencyUtils
import com.example.ui.viewmodel.ControlTab
import com.example.ui.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RestaurantViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allStockItems by viewModel.allStockItems.collectAsStateWithLifecycle()
    val criticalStockItems by viewModel.criticalStockItems.collectAsStateWithLifecycle()
    val allPurchases by viewModel.allPurchases.collectAsStateWithLifecycle()
    val purchasesEnCamino by viewModel.purchasesEnCamino.collectAsStateWithLifecycle()
    val purchasesFaltantes by viewModel.purchasesFaltantes.collectAsStateWithLifecycle()
    val allMovements by viewModel.allMovements.collectAsStateWithLifecycle()
    val exchangeRates by viewModel.exchangeRates.collectAsStateWithLifecycle()

    val usdRate = CurrencyUtils.getRateFor("USD", exchangeRates)

    // Back handling
    BackHandler(enabled = currentTab != ControlTab.PANEL_DUENO) {
        viewModel.setTab(ControlTab.PANEL_DUENO)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Control Lachy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Control de Inventario & Almacén (1 USD = ${usdRate.toInt()} CUP)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                // Tab 1: Panel Dueño
                NavigationBarItem(
                    selected = currentTab == ControlTab.PANEL_DUENO,
                    onClick = { viewModel.setTab(ControlTab.PANEL_DUENO) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Panel Dueño") },
                    label = { Text("Dueño", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_owner")
                )

                // Tab 2: Inventario & Stock
                val criticalCount = criticalStockItems.size
                NavigationBarItem(
                    selected = currentTab == ControlTab.INVENTARIO,
                    onClick = { viewModel.setTab(ControlTab.INVENTARIO) },
                    icon = {
                        if (criticalCount > 0) {
                            BadgedBox(badge = { Badge { Text("$criticalCount") } }) {
                                Icon(Icons.Default.Inventory2, contentDescription = "Inventario")
                            }
                        } else {
                            Icon(Icons.Default.Inventory2, contentDescription = "Inventario")
                        }
                    },
                    label = { Text("Inventario", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_inventory")
                )

                // Tab 3: Comprador & Entradas
                val enCaminoCount = purchasesEnCamino.size
                NavigationBarItem(
                    selected = currentTab == ControlTab.COMPRADOR,
                    onClick = { viewModel.setTab(ControlTab.COMPRADOR) },
                    icon = {
                        if (enCaminoCount > 0) {
                            BadgedBox(badge = { Badge { Text("$enCaminoCount") } }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Comprador")
                            }
                        } else {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Comprador")
                        }
                    },
                    label = { Text("Comprador", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_buyer")
                )

                // Tab 4: Salidas & Historial
                NavigationBarItem(
                    selected = currentTab == ControlTab.HISTORIAL,
                    onClick = { viewModel.setTab(ControlTab.HISTORIAL) },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Kárdex", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_kardex")
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
                ControlTab.PANEL_DUENO -> {
                    OwnerDashboardScreen(
                        stockItems = allStockItems,
                        criticalItems = criticalStockItems,
                        purchasesEnCamino = purchasesEnCamino,
                        recentMovements = allMovements,
                        exchangeRates = exchangeRates,
                        onNavigateToInventory = { viewModel.setTab(ControlTab.INVENTARIO) },
                        onNavigateToPurchases = { viewModel.setTab(ControlTab.COMPRADOR) },
                        onNavigateToMovements = { viewModel.setTab(ControlTab.HISTORIAL) }
                    )
                }

                ControlTab.INVENTARIO -> {
                    InventoryScreen(
                        stockItems = allStockItems,
                        onAdjustStock = { itemId, newStock, responsible, reason ->
                            viewModel.quickAdjustStock(itemId, newStock, responsible, reason)
                        },
                        onRegisterOutput = { itemId, qty, responsible, reason, notes ->
                            viewModel.registerStockOutput(itemId, qty, responsible, reason, notes)
                        },
                        onRegisterDirectEntry = { itemId, qty, unitPrice, buyer, place, notes ->
                            val item = allStockItems.find { it.id == itemId }
                            if (item != null) {
                                viewModel.registerPurchase(
                                    purchaseId = null,
                                    itemName = item.name,
                                    quantity = qty,
                                    unit = item.unit,
                                    unitPriceCup = unitPrice,
                                    buyerName = buyer,
                                    purchasePlace = place,
                                    directlyReceived = true,
                                    notes = notes
                                )
                            }
                        },
                        onSaveItem = { item ->
                            viewModel.saveStockItem(item)
                        },
                        onDeleteItem = { item ->
                            viewModel.deleteStockItem(item)
                        }
                    )
                }

                ControlTab.COMPRADOR -> {
                    BuyerPurchasesScreen(
                        purchasesEnCamino = purchasesEnCamino,
                        purchasesFaltantes = purchasesFaltantes,
                        criticalItems = criticalStockItems,
                        allItems = allStockItems,
                        allPurchases = allPurchases,
                        onRegisterPurchase = { purchaseId, itemName, qty, unit, unitPrice, buyer, place, directlyReceived, notes ->
                            viewModel.registerPurchase(purchaseId, itemName, qty, unit, unitPrice, buyer, place, directlyReceived, notes)
                        },
                        onConfirmReceipt = { purchase, receiver ->
                            viewModel.confirmReceipt(purchase, receiver)
                        }
                    )
                }

                ControlTab.HISTORIAL -> {
                    StockMovementsScreen(
                        movements = allMovements,
                        stockItems = allStockItems,
                        onRegisterOutput = { itemId, qty, responsible, reason, notes ->
                            viewModel.registerStockOutput(itemId, qty, responsible, reason, notes)
                        }
                    )
                }
            }
        }
    }
}
