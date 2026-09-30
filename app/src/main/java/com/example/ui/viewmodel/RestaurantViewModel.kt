package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.RestaurantDatabase
import com.example.data.model.ExchangeRate
import com.example.data.model.Expense
import com.example.data.model.ItemCookingStatus
import com.example.data.model.MenuItem
import com.example.data.model.OrderItem
import com.example.data.model.OrderWithItems
import com.example.data.model.TableEntity
import com.example.data.repository.RestaurantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    TABLES("Mesas"),
    POS("Comanda"),
    INVENTORY("Inventario"),
    CASH("Caja & Cuadre"),
    RATES("Tasas Cuba")
}

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RestaurantRepository

    init {
        val database = RestaurantDatabase.getDatabase(application, viewModelScope)
        repository = RestaurantRepository(database.restaurantDao())
    }

    // Active Navigation
    private val _currentTab = MutableStateFlow(AppTab.TABLES)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Selected Table & Active Order
    private val _selectedTableId = MutableStateFlow<Long?>(null)
    val selectedTableId: StateFlow<Long?> = _selectedTableId.asStateFlow()

    private val _selectedOrderId = MutableStateFlow<Long?>(null)
    val selectedOrderId: StateFlow<Long?> = _selectedOrderId.asStateFlow()

    // State flows from Repository
    val tables: StateFlow<List<TableEntity>> = repository.allTables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val menuItems: StateFlow<List<MenuItem>> = repository.allMenuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exchangeRates: StateFlow<List<ExchangeRate>> = repository.allExchangeRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closedOrders: StateFlow<List<OrderWithItems>> = repository.closedOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Order With Items
    val activeOrderWithItems: StateFlow<OrderWithItems?> = _selectedOrderId
        .flatMapLatest { orderId ->
            if (orderId != null) {
                repository.getOrderWithItems(orderId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Waiter default name
    private val _waiterName = MutableStateFlow("Mesero")
    val waiterName: StateFlow<String> = _waiterName.asStateFlow()

    fun setWaiterName(name: String) {
        _waiterName.value = name
    }

    // Actions
    fun selectTable(table: TableEntity) {
        _selectedTableId.value = table.id
        _selectedOrderId.value = table.activeOrderId
        if (table.activeOrderId != null) {
            _currentTab.value = AppTab.POS
        }
    }

    fun openTable(tableId: Long, waiter: String, diners: Int, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val orderId = repository.openTable(tableId, waiter, diners)
            if (orderId > 0) {
                _selectedTableId.value = tableId
                _selectedOrderId.value = orderId
                _currentTab.value = AppTab.POS
                onSuccess(orderId)
            }
        }
    }

    fun addItemToActiveOrder(menuItem: MenuItem, quantity: Int = 1, notes: String = "") {
        val orderId = _selectedOrderId.value ?: return
        viewModelScope.launch {
            repository.addItemToOrder(orderId, menuItem, quantity, notes)
        }
    }

    fun removeItemFromOrder(item: OrderItem) {
        viewModelScope.launch {
            repository.removeItemFromOrder(item)
        }
    }

    fun updateItemStatus(itemId: Long, status: ItemCookingStatus) {
        viewModelScope.launch {
            repository.updateItemStatus(itemId, status)
        }
    }

    fun updateOrderServiceCharge(orderId: Long, servicePercent: Double, discountPercent: Double = 0.0) {
        viewModelScope.launch {
            repository.updateOrderServiceAndDiscount(orderId, servicePercent, discountPercent)
        }
    }

    fun requestBillForCurrentTable() {
        val tableId = _selectedTableId.value ?: return
        viewModelScope.launch {
            repository.requestBillForTable(tableId)
        }
    }

    fun checkoutCurrentOrder(
        paidCup: Double,
        paidUsd: Double,
        paidMlc: Double,
        paidEur: Double,
        paymentMethod: String,
        changeCup: Double,
        notes: String = ""
    ) {
        val orderId = _selectedOrderId.value ?: return
        val tableId = _selectedTableId.value ?: return

        viewModelScope.launch {
            repository.closeOrderAndFreeTable(
                orderId = orderId,
                tableId = tableId,
                paidCup = paidCup,
                paidUsd = paidUsd,
                paidMlc = paidMlc,
                paidEur = paidEur,
                paymentMethod = paymentMethod,
                changeCup = changeCup,
                notes = notes
            )
            _selectedOrderId.value = null
            _selectedTableId.value = null
            _currentTab.value = AppTab.TABLES
        }
    }

    fun cancelActiveOrder() {
        val orderId = _selectedOrderId.value ?: return
        val tableId = _selectedTableId.value ?: return
        viewModelScope.launch {
            repository.cancelActiveOrder(orderId, tableId)
            _selectedOrderId.value = null
            _selectedTableId.value = null
            _currentTab.value = AppTab.TABLES
        }
    }

    // Inventory operations
    fun adjustStock(itemId: Long, delta: Int) {
        viewModelScope.launch {
            repository.adjustStock(itemId, delta)
        }
    }

    fun saveMenuItem(item: MenuItem) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertMenuItem(item)
            } else {
                repository.updateMenuItem(item)
            }
        }
    }

    fun deleteMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
        }
    }

    // Exchange rate operations
    fun updateExchangeRate(code: String, rate: Double) {
        viewModelScope.launch {
            repository.updateExchangeRate(code, rate)
        }
    }

    // Expenses operations
    fun addExpense(description: String, amountCup: Double, category: String, notes: String) {
        viewModelScope.launch {
            repository.addExpense(description, amountCup, category, notes)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Add table
    fun addTable(name: String, area: String, capacity: Int) {
        viewModelScope.launch {
            repository.insertTable(
                TableEntity(
                    name = name,
                    area = area,
                    capacity = capacity
                )
            )
        }
    }
}
