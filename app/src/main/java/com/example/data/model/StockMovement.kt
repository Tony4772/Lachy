package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MovementType(val label: String) {
    ENTRADA_COMPRA("Entrada por Compra"),
    SALIDA_DESPACHO("Salida / Despacho"),
    SALIDA_CONSUMO("Salida por Consumo"),
    SALIDA_MERMA("Merma o Pérdida"),
    AJUSTE_MANUAL("Ajuste de Conteo Físico")
}

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stockItemId: Long,
    val itemName: String,
    val type: MovementType,
    val quantity: Double,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val registeredBy: String = "Almacén",
    val notes: String = ""
)
