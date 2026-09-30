package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ExchangeRate
import com.example.data.model.Expense
import com.example.data.model.ItemCookingStatus
import com.example.data.model.MenuItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItem
import com.example.data.model.OrderWithItems
import com.example.data.model.TableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    // --- MENU ITEMS ---
    @Query("SELECT * FROM menu_items ORDER BY category ASC, name ASC")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_items WHERE isAvailable = 1 ORDER BY category ASC, name ASC")
    fun getAvailableMenuItems(): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun getMenuItemById(id: Long): MenuItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItem>)

    @Update
    suspend fun updateMenuItem(item: MenuItem)

    @Delete
    suspend fun deleteMenuItem(item: MenuItem)

    @Query("UPDATE menu_items SET stock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int)

    // --- TABLES ---
    @Query("SELECT * FROM tables ORDER BY id ASC")
    fun getAllTables(): Flow<List<TableEntity>>

    @Query("SELECT * FROM tables WHERE id = :id")
    suspend fun getTableById(id: Long): TableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: TableEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<TableEntity>)

    @Update
    suspend fun updateTable(table: TableEntity)

    // --- ORDERS ---
    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE tableId = :tableId AND isClosed = 0 LIMIT 1")
    suspend fun getActiveOrderForTable(tableId: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderWithItems(orderId: Long): Flow<OrderWithItems?>

    @Transaction
    @Query("SELECT * FROM orders WHERE isClosed = 1 ORDER BY closedAt DESC")
    fun getClosedOrders(): Flow<List<OrderWithItems>>

    @Query("SELECT * FROM orders WHERE isClosed = 1 AND closedAt >= :startOfDay AND closedAt <= :endOfDay ORDER BY closedAt DESC")
    fun getClosedOrdersForDate(startOfDay: Long, endOfDay: Long): Flow<List<OrderEntity>>

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    // --- ORDER ITEMS ---
    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY addedAt ASC")
    fun getItemsForOrder(orderId: Long): Flow<List<OrderItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: OrderItem): Long

    @Update
    suspend fun updateOrderItem(item: OrderItem)

    @Delete
    suspend fun deleteOrderItem(item: OrderItem)

    @Query("DELETE FROM order_items WHERE id = :id")
    suspend fun deleteOrderItemById(id: Long)

    @Query("UPDATE order_items SET status = :status WHERE id = :itemId")
    suspend fun updateItemCookingStatus(itemId: Long, status: ItemCookingStatus)

    // --- EXCHANGE RATES ---
    @Query("SELECT * FROM exchange_rates")
    fun getAllExchangeRates(): Flow<List<ExchangeRate>>

    @Query("SELECT * FROM exchange_rates WHERE currencyCode = :code LIMIT 1")
    suspend fun getExchangeRateByCode(code: String): ExchangeRate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateExchangeRate(rate: ExchangeRate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(rates: List<ExchangeRate>)

    // --- EXPENSES ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getExpensesForDate(startOfDay: Long, endOfDay: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
