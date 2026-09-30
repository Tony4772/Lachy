package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val menuItemId: Long,
    val name: String,
    val category: String,
    val priceCup: Double,
    val quantity: Int = 1,
    val notes: String = "",
    val status: ItemCookingStatus = ItemCookingStatus.PENDING,
    val addedAt: Long = System.currentTimeMillis()
) {
    val totalCup: Double get() = priceCup * quantity
}

enum class ItemCookingStatus(val label: String) {
    PENDING("Pendiente"),
    COOKING("En Cocina"),
    SERVED("Servido")
}
