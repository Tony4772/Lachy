package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tableId: Long,
    val tableName: String,
    val waiterName: String = "Lachy Rest",
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val isClosed: Boolean = false,
    val servicePercent: Double = 10.0, // 10% propina / servicio tradicional en Cuba
    val discountPercent: Double = 0.0,
    val subtotalCup: Double = 0.0,
    val totalCup: Double = 0.0,
    val paymentMethod: String = "Efectivo CUP", // "Efectivo CUP", "USD", "MLC Transfermóvil", "Mixto"
    val paidCup: Double = 0.0,
    val paidUsd: Double = 0.0,
    val paidMlc: Double = 0.0,
    val paidEur: Double = 0.0,
    val changeCup: Double = 0.0,
    val notes: String = ""
)
