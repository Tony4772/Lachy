package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "General",
    val unit: String = "Unidades",
    val currentStock: Double = 0.0,
    val minStockAlert: Double = 5.0,
    val lastUnitPriceCup: Double = 0.0,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isCritical: Boolean get() = currentStock <= minStockAlert && currentStock > 0.0
    val isOutOfStock: Boolean get() = currentStock <= 0.0
    val totalValueCup: Double get() = (currentStock * lastUnitPriceCup).coerceAtLeast(0.0)
}
