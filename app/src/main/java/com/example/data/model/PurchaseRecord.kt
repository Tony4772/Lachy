package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PurchaseStatus(val label: String) {
    NECESITA_COMPRAR("Por Comprar"),
    COMPRADO_EN_CAMINO("Comprado (En Camino)"),
    RECIBIDO_EN_ALMACEN("Ingresado al Almacén")
}

@Entity(tableName = "purchases")
data class PurchaseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stockItemId: Long? = null,
    val itemName: String,
    val quantity: Double,
    val unit: String = "Libras",
    val unitPriceCup: Double = 0.0,
    val totalCostCup: Double = 0.0,
    val currencyUsed: String = "CUP",
    val buyerName: String = "Comprador",
    val purchasePlace: String = "Mercado / Proveedor",
    val status: PurchaseStatus = PurchaseStatus.COMPRADO_EN_CAMINO,
    val requestedAt: Long = System.currentTimeMillis(),
    val purchasedAt: Long? = System.currentTimeMillis(),
    val receivedAt: Long? = null,
    val receivedBy: String? = null,
    val notes: String = ""
)
