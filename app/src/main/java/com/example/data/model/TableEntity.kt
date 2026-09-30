package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "Mesa 1", "Mesa 2", "Terraza 1", "Barra 1", "Para Llevar"
    val area: String, // "Salón", "Terraza", "Barra", "Para Llevar"
    val capacity: Int = 4,
    val status: TableStatus = TableStatus.FREE,
    val activeOrderId: Long? = null,
    val currentWaiter: String = "Mesero",
    val dinersCount: Int = 2,
    val openedAt: Long = 0L
)

enum class TableStatus {
    FREE,           // Libre (Verde)
    OCCUPIED,       // Ocupada / Comanda activa (Azul)
    BILL_REQUESTED  // Cuenta solicitada / Por cobrar (Ámbar)
}
