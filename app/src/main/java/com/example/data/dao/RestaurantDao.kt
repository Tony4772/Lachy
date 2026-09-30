package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExchangeRate
import com.example.data.model.PurchaseRecord
import com.example.data.model.PurchaseStatus
import com.example.data.model.StockItem
import com.example.data.model.StockMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    // --- STOCK / INVENTARIO ALMACÉN ---
    @Query("SELECT * FROM stock_items ORDER BY category ASC, name ASC")
    fun getAllStockItems(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE currentStock <= minStockAlert ORDER BY currentStock ASC")
    fun getCriticalStockItems(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE id = :id LIMIT 1")
    suspend fun getStockItemById(id: Long): StockItem?

    @Query("SELECT * FROM stock_items WHERE name = :name LIMIT 1")
    suspend fun getStockItemByName(name: String): StockItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItem(item: StockItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItems(items: List<StockItem>)

    @Update
    suspend fun updateStockItem(item: StockItem)

    @Delete
    suspend fun deleteStockItem(item: StockItem)

    @Query("UPDATE stock_items SET currentStock = :newStock, lastUnitPriceCup = CASE WHEN :lastPrice > 0 THEN :lastPrice ELSE lastUnitPriceCup END, updatedAt = :time WHERE id = :id")
    suspend fun updateStockQuantity(id: Long, newStock: Double, lastPrice: Double, time: Long)

    // --- COMPRAS & LOGÍSTICA ---
    @Query("SELECT * FROM purchases ORDER BY requestedAt DESC, id DESC")
    fun getAllPurchases(): Flow<List<PurchaseRecord>>

    @Query("SELECT * FROM purchases WHERE status = :status ORDER BY requestedAt DESC")
    fun getPurchasesByStatus(status: PurchaseStatus): Flow<List<PurchaseRecord>>

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    suspend fun getPurchaseById(id: Long): PurchaseRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<PurchaseRecord>)

    @Update
    suspend fun updatePurchase(purchase: PurchaseRecord)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseRecord)

    // --- MOVIMIENTOS KÁRDEX ---
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE stockItemId = :itemId ORDER BY timestamp DESC")
    fun getMovementsForItem(itemId: Long): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovement): Long

    // --- TASAS DE CAMBIO ---
    @Query("SELECT * FROM exchange_rates")
    fun getAllExchangeRates(): Flow<List<ExchangeRate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(rates: List<ExchangeRate>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateExchangeRate(rate: ExchangeRate)
}
