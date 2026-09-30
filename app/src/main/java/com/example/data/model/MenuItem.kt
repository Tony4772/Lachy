package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // Platos Fuertes, Guarniciones, Entrantes, Bebidas, Coctelería, Postres
    val priceCup: Double,
    val costCup: Double = 0.0,
    val stock: Int = 0,
    val trackStock: Boolean = true,
    val isAvailable: Boolean = true,
    val description: String = ""
)

enum class MenuCategory(val displayName: String, val iconName: String) {
    PLATOS_FUERTES("Platos Fuertes", "restaurant"),
    ENTRANTES("Entrantes", "tapas"),
    GUARNICIONES("Guarniciones", "rice_bowl"),
    BEBIDAS("Bebidas", "sports_bar"),
    COCTELERIA("Coctelería", "local_bar"),
    POSTRES("Postres", "icecream"),
    OTROS("Otros", "more_horiz")
}
