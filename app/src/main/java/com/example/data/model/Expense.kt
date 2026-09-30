package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val amountCup: Double,
    val category: String = "Insumos", // "Agromercado / Viandas", "Hielo / Refrescos", "Carnicería", "Carbón / Gas", "Salario / Anticipo", "Mantenimiento", "Otros"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
