package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.RestaurantDatabase
import com.example.data.model.ExchangeRate
import com.example.data.model.MovementType
import com.example.data.model.PurchaseRecord
import com.example.data.model.StockItem
import com.example.data.model.StockMovement
import com.example.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ControlTab(val title: String) {
    PANEL_DUENO("Panel Dueño"),
    INVENTARIO("Almacén & Stock"),
    COMPRADOR("Comprador & Entradas"),
    HISTORIAL("Salidas & Kárdex")
}

class RestaurantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RestaurantRepository

    init {
        val database = RestaurantDatabase.getDatabase(application, viewModelScope)
        repository = RestaurantRepository(database.restaurantDao())
    }

    private val _currentTab = MutableStateFlow(ControlTab.PANEL_DUENO)
    val currentTab: StateFlow<ControlTab> = _currentTab.asStateFlow()

    fun setTab(tab: ControlTab) {
        _currentTab.value = tab
    }

    // States from Repository
    val allStockItems: StateFlow<List<StockItem>> = repository.allStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val criticalStockItems: StateFlow<List<StockItem>> = repository.criticalStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchases: StateFlow<List<PurchaseRecord>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasesEnCamino: StateFlow<List<PurchaseRecord>> = repository.purchasesEnCamino
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasesFaltantes: StateFlow<List<PurchaseRecord>> = repository.purchasesFaltantes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMovements: StateFlow<List<StockMovement>> = repository.allMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exchangeRates: StateFlow<List<ExchangeRate>> = repository.allExchangeRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun requestPurchase(itemName: String, quantity: Double, unit: String, notes: String) {
        viewModelScope.launch {
            repository.requestPurchase(itemName, quantity, unit, notes)
        }
    }

    fun registerPurchase(
        purchaseId: Long? = null,
        itemName: String,
        quantity: Double,
        unit: String,
        unitPriceCup: Double,
        buyerName: String,
        purchasePlace: String,
        directlyReceived: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            repository.registerPurchaseByBuyer(
                purchaseId = purchaseId,
                itemName = itemName,
                quantity = quantity,
                unit = unit,
                unitPriceCup = unitPriceCup,
                buyerName = buyerName,
                purchasePlace = purchasePlace,
                directlyReceived = directlyReceived,
                notes = notes
            )
        }
    }

    fun confirmReceipt(purchase: PurchaseRecord, receiverName: String) {
        viewModelScope.launch {
            repository.confirmReceiptAtWarehouse(purchase, receiverName)
        }
    }

    fun registerStockOutput(
        itemId: Long,
        quantity: Double,
        responsible: String,
        reason: MovementType,
        notes: String
    ) {
        viewModelScope.launch {
            repository.registerStockOutput(itemId, quantity, responsible, reason, notes)
        }
    }

    fun quickAdjustStock(
        itemId: Long,
        newStock: Double,
        responsible: String,
        reason: String
    ) {
        viewModelScope.launch {
            repository.quickAdjustStock(itemId, newStock, responsible, reason)
        }
    }

    fun saveStockItem(item: StockItem) {
        viewModelScope.launch {
            repository.addOrUpdateStockItem(item)
        }
    }

    fun deleteStockItem(item: StockItem) {
        viewModelScope.launch {
            repository.deleteStockItem(item)
        }
    }

    fun updateExchangeRate(code: String, rate: Double) {
        viewModelScope.launch {
            repository.updateExchangeRate(code, rate)
        }
    }
}
