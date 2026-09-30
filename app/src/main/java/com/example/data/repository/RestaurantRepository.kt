package com.example.data.repository

import com.example.data.dao.RestaurantDao
import com.example.data.model.ExchangeRate
import com.example.data.model.MovementType
import com.example.data.model.PurchaseRecord
import com.example.data.model.PurchaseStatus
import com.example.data.model.StockItem
import com.example.data.model.StockMovement
import kotlinx.coroutines.flow.Flow

class RestaurantRepository(private val dao: RestaurantDao) {

    // --- STOCK / INVENTARIO ---
    val allStockItems: Flow<List<StockItem>> = dao.getAllStockItems()
    val criticalStockItems: Flow<List<StockItem>> = dao.getCriticalStockItems()

    suspend fun getStockItemById(id: Long): StockItem? = dao.getStockItemById(id)

    suspend fun addOrUpdateStockItem(item: StockItem): Long {
        return if (item.id == 0L) {
            dao.insertStockItem(item)
        } else {
            dao.updateStockItem(item)
            item.id
        }
    }

    suspend fun deleteStockItem(item: StockItem) = dao.deleteStockItem(item)

    // --- COMPRAS & ENTRADAS AL ALMACÉN ---
    val allPurchases: Flow<List<PurchaseRecord>> = dao.getAllPurchases()
    val purchasesEnCamino: Flow<List<PurchaseRecord>> = dao.getPurchasesByStatus(PurchaseStatus.COMPRADO_EN_CAMINO)
    val purchasesFaltantes: Flow<List<PurchaseRecord>> = dao.getPurchasesByStatus(PurchaseStatus.NECESITA_COMPRAR)

    suspend fun requestPurchase(
        itemName: String,
        quantity: Double,
        unit: String,
        notes: String
    ): Long {
        val existingItem = dao.getStockItemByName(itemName)
        return dao.insertPurchase(
            PurchaseRecord(
                stockItemId = existingItem?.id,
                itemName = itemName,
                quantity = quantity,
                unit = unit,
                unitPriceCup = existingItem?.lastUnitPriceCup ?: 0.0,
                totalCostCup = (existingItem?.lastUnitPriceCup ?: 0.0) * quantity,
                status = PurchaseStatus.NECESITA_COMPRAR,
                requestedAt = System.currentTimeMillis(),
                purchasedAt = null,
                notes = notes
            )
        )
    }

    suspend fun registerPurchaseByBuyer(
        purchaseId: Long?,
        itemName: String,
        quantity: Double,
        unit: String,
        unitPriceCup: Double,
        buyerName: String,
        purchasePlace: String,
        directlyReceived: Boolean,
        notes: String
    ): Long {
        val existingItem = dao.getStockItemByName(itemName)
        val totalCost = unitPriceCup * quantity
        val now = System.currentTimeMillis()

        val status = if (directlyReceived) PurchaseStatus.RECIBIDO_EN_ALMACEN else PurchaseStatus.COMPRADO_EN_CAMINO

        val targetPurchaseId: Long = if (purchaseId != null && purchaseId > 0) {
            val existing = dao.getPurchaseById(purchaseId)
            if (existing != null) {
                dao.updatePurchase(
                    existing.copy(
                        quantity = quantity,
                        unit = unit,
                        unitPriceCup = unitPriceCup,
                        totalCostCup = totalCost,
                        buyerName = buyerName,
                        purchasePlace = purchasePlace,
                        status = status,
                        purchasedAt = now,
                        receivedAt = if (directlyReceived) now else null,
                        receivedBy = if (directlyReceived) buyerName else null,
                        notes = notes
                    )
                )
                purchaseId
            } else {
                dao.insertPurchase(
                    PurchaseRecord(
                        stockItemId = existingItem?.id,
                        itemName = itemName,
                        quantity = quantity,
                        unit = unit,
                        unitPriceCup = unitPriceCup,
                        totalCostCup = totalCost,
                        buyerName = buyerName,
                        purchasePlace = purchasePlace,
                        status = status,
                        purchasedAt = now,
                        receivedAt = if (directlyReceived) now else null,
                        receivedBy = if (directlyReceived) buyerName else null,
                        notes = notes
                    )
                )
            }
        } else {
            dao.insertPurchase(
                PurchaseRecord(
                    stockItemId = existingItem?.id,
                    itemName = itemName,
                    quantity = quantity,
                    unit = unit,
                    unitPriceCup = unitPriceCup,
                    totalCostCup = totalCost,
                    buyerName = buyerName,
                    purchasePlace = purchasePlace,
                    status = status,
                    purchasedAt = now,
                    receivedAt = if (directlyReceived) now else null,
                    receivedBy = if (directlyReceived) buyerName else null,
                    notes = notes
                )
            )
        }

        // If directly received into warehouse, update stock immediately
        if (directlyReceived) {
            val item = existingItem ?: dao.getStockItemById(
                dao.insertStockItem(
                    StockItem(
                        name = itemName,
                        category = "General",
                        unit = unit,
                        currentStock = 0.0,
                        lastUnitPriceCup = unitPriceCup
                    )
                )
            )

            if (item != null) {
                val newStock = item.currentStock + quantity
                dao.updateStockQuantity(item.id, newStock, unitPriceCup, now)
                dao.insertMovement(
                    StockMovement(
                        stockItemId = item.id,
                        itemName = item.name,
                        type = MovementType.ENTRADA_COMPRA,
                        quantity = quantity,
                        unit = unit,
                        timestamp = now,
                        registeredBy = buyerName,
                        notes = "Entrada por compra directa de $buyerName ($purchasePlace)"
                    )
                )
            }
        }

        return targetPurchaseId
    }

    suspend fun confirmReceiptAtWarehouse(
        purchase: PurchaseRecord,
        receiverName: String
    ) {
        val now = System.currentTimeMillis()

        // 1. Mark purchase as received in warehouse
        dao.updatePurchase(
            purchase.copy(
                status = PurchaseStatus.RECIBIDO_EN_ALMACEN,
                receivedAt = now,
                receivedBy = receiverName
            )
        )

        // 2. Find or create stock item
        val item = if (purchase.stockItemId != null) {
            dao.getStockItemById(purchase.stockItemId)
        } else {
            dao.getStockItemByName(purchase.itemName)
        } ?: run {
            val newId = dao.insertStockItem(
                StockItem(
                    name = purchase.itemName,
                    category = "General",
                    unit = purchase.unit,
                    currentStock = 0.0,
                    lastUnitPriceCup = purchase.unitPriceCup
                )
            )
            dao.getStockItemById(newId)
        }

        if (item != null) {
            val newStock = item.currentStock + purchase.quantity
            dao.updateStockQuantity(item.id, newStock, purchase.unitPriceCup, now)

            // 3. Log movement
            dao.insertMovement(
                StockMovement(
                    stockItemId = item.id,
                    itemName = item.name,
                    type = MovementType.ENTRADA_COMPRA,
                    quantity = purchase.quantity,
                    unit = item.unit,
                    timestamp = now,
                    registeredBy = receiverName,
                    notes = "Ingreso a almacén de compra ($purchase.purchasePlace por ${purchase.buyerName})"
                )
            )
        }
    }

    suspend fun registerStockOutput(
        itemId: Long,
        quantity: Double,
        responsible: String,
        reason: MovementType,
        notes: String
    ) {
        val item = dao.getStockItemById(itemId) ?: return
        val newStock = (item.currentStock - quantity).coerceAtLeast(0.0)
        val now = System.currentTimeMillis()

        dao.updateStockQuantity(itemId, newStock, item.lastUnitPriceCup, now)

        dao.insertMovement(
            StockMovement(
                stockItemId = itemId,
                itemName = item.name,
                type = reason,
                quantity = -quantity,
                unit = item.unit,
                timestamp = now,
                registeredBy = responsible,
                notes = notes
            )
        )
    }

    suspend fun quickAdjustStock(
        itemId: Long,
        newStock: Double,
        responsible: String,
        reason: String
    ) {
        val item = dao.getStockItemById(itemId) ?: return
        val now = System.currentTimeMillis()
        val diff = newStock - item.currentStock

        dao.updateStockQuantity(itemId, newStock, item.lastUnitPriceCup, now)

        dao.insertMovement(
            StockMovement(
                stockItemId = itemId,
                itemName = item.name,
                type = MovementType.AJUSTE_MANUAL,
                quantity = diff,
                unit = item.unit,
                timestamp = now,
                registeredBy = responsible,
                notes = "Ajuste de conteo físico: $reason"
            )
        )
    }

    // --- KÁRDEX & MOVIMIENTOS ---
    val allMovements: Flow<List<StockMovement>> = dao.getAllMovements()

    // --- TASAS DE CAMBIO ---
    val allExchangeRates: Flow<List<ExchangeRate>> = dao.getAllExchangeRates()

    suspend fun updateExchangeRate(code: String, newRate: Double) {
        dao.insertOrUpdateExchangeRate(
            ExchangeRate(
                currencyCode = code,
                rateToCup = newRate,
                symbol = when (code) {
                    "USD" -> "$"
                    "EUR" -> "€"
                    "MLC" -> "MLC"
                    else -> "$"
                },
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
