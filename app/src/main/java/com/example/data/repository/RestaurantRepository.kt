package com.example.data.repository

import com.example.data.dao.RestaurantDao
import com.example.data.model.ExchangeRate
import com.example.data.model.Expense
import com.example.data.model.ItemCookingStatus
import com.example.data.model.MenuItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItem
import com.example.data.model.OrderWithItems
import com.example.data.model.TableEntity
import com.example.data.model.TableStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class RestaurantRepository(private val dao: RestaurantDao) {

    // --- Mesas ---
    val allTables: Flow<List<TableEntity>> = dao.getAllTables()

    suspend fun getTableById(id: Long): TableEntity? = dao.getTableById(id)

    suspend fun updateTable(table: TableEntity) = dao.updateTable(table)

    suspend fun insertTable(table: TableEntity): Long = dao.insertTable(table)

    // --- Menú & Inventario ---
    val allMenuItems: Flow<List<MenuItem>> = dao.getAllMenuItems()
    val availableMenuItems: Flow<List<MenuItem>> = dao.getAvailableMenuItems()

    suspend fun insertMenuItem(item: MenuItem): Long = dao.insertMenuItem(item)

    suspend fun updateMenuItem(item: MenuItem) = dao.updateMenuItem(item)

    suspend fun deleteMenuItem(item: MenuItem) = dao.deleteMenuItem(item)

    suspend fun updateStock(id: Long, newStock: Int) = dao.updateStock(id, newStock)

    suspend fun adjustStock(id: Long, delta: Int) {
        val item = dao.getMenuItemById(id) ?: return
        val updated = (item.stock + delta).coerceAtLeast(0)
        dao.updateStock(id, updated)
    }

    // --- Tasas de Cambio ---
    val allExchangeRates: Flow<List<ExchangeRate>> = dao.getAllExchangeRates()

    suspend fun updateExchangeRate(code: String, newRate: Double) {
        val existing = dao.getExchangeRateByCode(code)
        val symbol = existing?.symbol ?: when (code) {
            "USD" -> "$"
            "EUR" -> "€"
            "MLC" -> "MLC"
            else -> "$"
        }
        dao.insertOrUpdateExchangeRate(
            ExchangeRate(
                currencyCode = code,
                rateToCup = newRate,
                symbol = symbol,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // --- Gastos ---
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()

    suspend fun addExpense(description: String, amountCup: Double, category: String, notes: String): Long {
        return dao.insertExpense(
            Expense(
                description = description,
                amountCup = amountCup,
                category = category,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    // --- Comandas / Órdenes ---
    fun getOrderWithItems(orderId: Long): Flow<OrderWithItems?> = dao.getOrderWithItems(orderId)

    val closedOrders: Flow<List<OrderWithItems>> = dao.getClosedOrders()

    suspend fun openTable(tableId: Long, waiter: String, diners: Int): Long {
        val table = dao.getTableById(tableId) ?: return -1
        // Create new active order
        val orderId = dao.insertOrder(
            OrderEntity(
                tableId = tableId,
                tableName = table.name,
                waiterName = waiter.ifBlank { "Mesero" },
                openedAt = System.currentTimeMillis(),
                servicePercent = 10.0 // 10% propina sugerida
            )
        )
        // Mark table occupied
        dao.updateTable(
            table.copy(
                status = TableStatus.OCCUPIED,
                activeOrderId = orderId,
                currentWaiter = waiter.ifBlank { "Mesero" },
                dinersCount = diners,
                openedAt = System.currentTimeMillis()
            )
        )
        return orderId
    }

    suspend fun addItemToOrder(orderId: Long, menuItem: MenuItem, quantity: Int, notes: String = "") {
        val order = dao.getOrderById(orderId) ?: return

        // Insert order item
        dao.insertOrderItem(
            OrderItem(
                orderId = orderId,
                menuItemId = menuItem.id,
                name = menuItem.name,
                category = menuItem.category,
                priceCup = menuItem.priceCup,
                quantity = quantity,
                notes = notes,
                status = ItemCookingStatus.PENDING
            )
        )

        // Decrement stock if item tracks stock
        if (menuItem.trackStock) {
            val currentStock = menuItem.stock
            val newStock = (currentStock - quantity).coerceAtLeast(0)
            dao.updateStock(menuItem.id, newStock)
        }

        recalculateOrderTotals(orderId, order.servicePercent, order.discountPercent)
    }

    suspend fun removeItemFromOrder(item: OrderItem) {
        dao.deleteOrderItem(item)
        // Restore stock
        if (item.menuItemId > 0) {
            val menuItem = dao.getMenuItemById(item.menuItemId)
            if (menuItem != null && menuItem.trackStock) {
                dao.updateStock(menuItem.id, menuItem.stock + item.quantity)
            }
        }
        val order = dao.getOrderById(item.orderId)
        if (order != null) {
            recalculateOrderTotals(item.orderId, order.servicePercent, order.discountPercent)
        }
    }

    suspend fun updateItemStatus(itemId: Long, status: ItemCookingStatus) {
        dao.updateItemCookingStatus(itemId, status)
    }

    suspend fun updateOrderServiceAndDiscount(orderId: Long, servicePercent: Double, discountPercent: Double) {
        recalculateOrderTotals(orderId, servicePercent, discountPercent)
    }

    private suspend fun recalculateOrderTotals(orderId: Long, servicePercent: Double, discountPercent: Double) {
        val order = dao.getOrderById(orderId) ?: return
        val items = dao.getItemsForOrder(orderId).firstOrNull() ?: emptyList()
        val subtotal = items.sumOf { it.priceCup * it.quantity }
        val discountAmount = subtotal * (discountPercent / 100.0)
        val serviceAmount = (subtotal - discountAmount) * (servicePercent / 100.0)
        val total = (subtotal - discountAmount) + serviceAmount

        dao.updateOrder(
            order.copy(
                subtotalCup = subtotal,
                servicePercent = servicePercent,
                discountPercent = discountPercent,
                totalCup = total
            )
        )
    }

    suspend fun requestBillForTable(tableId: Long) {
        val table = dao.getTableById(tableId) ?: return
        dao.updateTable(table.copy(status = TableStatus.BILL_REQUESTED))
    }

    suspend fun closeOrderAndFreeTable(
        orderId: Long,
        tableId: Long,
        paidCup: Double,
        paidUsd: Double,
        paidMlc: Double,
        paidEur: Double,
        paymentMethod: String,
        changeCup: Double,
        notes: String
    ) {
        val order = dao.getOrderById(orderId) ?: return
        val table = dao.getTableById(tableId)

        // Close order
        dao.updateOrder(
            order.copy(
                isClosed = true,
                closedAt = System.currentTimeMillis(),
                paidCup = paidCup,
                paidUsd = paidUsd,
                paidMlc = paidMlc,
                paidEur = paidEur,
                paymentMethod = paymentMethod,
                changeCup = changeCup,
                notes = notes
            )
        )

        // Free table
        if (table != null) {
            dao.updateTable(
                table.copy(
                    status = TableStatus.FREE,
                    activeOrderId = null,
                    dinersCount = 0,
                    openedAt = 0L
                )
            )
        }
    }

    suspend fun cancelActiveOrder(orderId: Long, tableId: Long) {
        val order = dao.getOrderById(orderId) ?: return
        val items = dao.getItemsForOrder(orderId).firstOrNull() ?: emptyList()

        // Restore stocks
        for (item in items) {
            val menuItem = dao.getMenuItemById(item.menuItemId)
            if (menuItem != null && menuItem.trackStock) {
                dao.updateStock(menuItem.id, menuItem.stock + item.quantity)
            }
        }

        dao.deleteOrder(order)

        val table = dao.getTableById(tableId)
        if (table != null) {
            dao.updateTable(
                table.copy(
                    status = TableStatus.FREE,
                    activeOrderId = null,
                    dinersCount = 0,
                    openedAt = 0L
                )
            )
        }
    }
}
