package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.RestaurantDao
import com.example.data.model.ExchangeRate
import com.example.data.model.Expense
import com.example.data.model.MenuItem
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItem
import com.example.data.model.TableEntity
import com.example.data.model.TableStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MenuItem::class,
        TableEntity::class,
        OrderEntity::class,
        OrderItem::class,
        ExchangeRate::class,
        Expense::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RestaurantDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao

    companion object {
        @Volatile
        private var INSTANCE: RestaurantDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): RestaurantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RestaurantDatabase::class.java,
                    "paladar_lachy_cienfuegos.db"
                )
                    .addCallback(DatabasePrepopulationCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabasePrepopulationCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        prepopulateData(database.restaurantDao())
                    }
                }
            }

            private suspend fun prepopulateData(dao: RestaurantDao) {
                // 1. Tasas de cambio iniciales (Cuba)
                val initialRates = listOf(
                    ExchangeRate(currencyCode = "USD", rateToCup = 330.0, symbol = "$"),
                    ExchangeRate(currencyCode = "MLC", rateToCup = 270.0, symbol = "MLC"),
                    ExchangeRate(currencyCode = "EUR", rateToCup = 345.0, symbol = "€")
                )
                dao.insertExchangeRates(initialRates)

                // 2. Mesas del Restaurante en Cienfuegos
                val initialTables = listOf(
                    TableEntity(name = "Mesa 1", area = "Salón Principal", capacity = 4, status = TableStatus.FREE),
                    TableEntity(name = "Mesa 2", area = "Salón Principal", capacity = 4, status = TableStatus.FREE),
                    TableEntity(name = "Mesa 3", area = "Salón Principal", capacity = 6, status = TableStatus.FREE),
                    TableEntity(name = "Mesa 4", area = "Salón Principal", capacity = 2, status = TableStatus.FREE),
                    TableEntity(name = "Terraza 1", area = "Terraza", capacity = 4, status = TableStatus.FREE),
                    TableEntity(name = "Terraza 2", area = "Terraza", capacity = 6, status = TableStatus.FREE),
                    TableEntity(name = "Barra 1", area = "Barra", capacity = 2, status = TableStatus.FREE),
                    TableEntity(name = "Barra 2", area = "Barra", capacity = 2, status = TableStatus.FREE),
                    TableEntity(name = "Para Llevar", area = "Para Llevar", capacity = 1, status = TableStatus.FREE)
                )
                dao.insertTables(initialTables)

                // 3. Menú típico de Paladar Cubana (Cienfuegos)
                val initialMenuItems = listOf(
                    // Platos Fuertes
                    MenuItem(name = "Ropa Vieja Criolla", category = "Platos Fuertes", priceCup = 1450.0, stock = 15, description = "Carne de res deshebrada en salsa criolla tradicional con pimientos"),
                    MenuItem(name = "Lechón Asado en Púa", category = "Platos Fuertes", priceCup = 1350.0, stock = 20, description = "Cerdo tierno asado con mojo de naranja agria y ajo"),
                    MenuItem(name = "Pescado a la Plancha (Perla del Sur)", category = "Platos Fuertes", priceCup = 1800.0, stock = 12, description = "Rueda de pargo o dorado fresco de la bahía de Cienfuegos"),
                    MenuItem(name = "Camarones Enchilados", category = "Platos Fuertes", priceCup = 2100.0, stock = 10, description = "Camarones en salsa picantita de tomate y especias criollas"),
                    MenuItem(name = "Filete de Cerdo Grillé", category = "Platos Fuertes", priceCup = 1200.0, stock = 18, description = "Acompañado de cebollas caramelizadas al limón"),
                    MenuItem(name = "Pollo Asado a la Cienfueguera", category = "Platos Fuertes", priceCup = 1100.0, stock = 16, description = "Cuarto de pollo adobado al carbón"),

                    // Entrantes / Tapas
                    MenuItem(name = "Frituras de Malanga", category = "Entrantes", priceCup = 450.0, stock = 30, description = "Crujientes con salsa de ajo y miel"),
                    MenuItem(name = "Croquetas Caseras (Jamonada)", category = "Entrantes", priceCup = 400.0, stock = 35, description = "Ración de 6 unidades doradas y cremosas"),
                    MenuItem(name = "Tostones Rellenos de Cerdo", category = "Entrantes", priceCup = 650.0, stock = 25, description = "Copitas de plátano verde rellenas de picadillo criollo"),
                    MenuItem(name = "Papas Bravas Criollas", category = "Entrantes", priceCup = 480.0, stock = 20, description = "Con alioli casero y toque picante"),

                    // Guarniciones
                    MenuItem(name = "Arroz Congrí Oriental", category = "Guarniciones", priceCup = 350.0, stock = 40, description = "Arroz con frijoles negros sazonado con comino y chicharrón"),
                    MenuItem(name = "Yuca con Mojo de Ajo", category = "Guarniciones", priceCup = 300.0, stock = 25, description = "Yuca suave con manteca de cerdo, naranja agria y ajo frito"),
                    MenuItem(name = "Tostones Chatinos", category = "Guarniciones", priceCup = 350.0, stock = 30, description = "Plátano verde aplastado y doble frito crujiente"),
                    MenuItem(name = "Ensalada de Estación", category = "Guarniciones", priceCup = 350.0, stock = 20, description = "Tomate, col, pepino y aguacate cuando hay"),

                    // Bebidas
                    MenuItem(name = "Cerveza Cristal Fría (Lata)", category = "Bebidas", priceCup = 450.0, stock = 48, description = "La preferida de Cuba, bien fría"),
                    MenuItem(name = "Cerveza Bucanero Fuerte (Lata)", category = "Bebidas", priceCup = 480.0, stock = 36, description = "Sabor más fuerte y cuerpo"),
                    MenuItem(name = "Refresco Tukola (Lata)", category = "Bebidas", priceCup = 320.0, stock = 40, description = "Refresco nacional de cola"),
                    MenuItem(name = "Malta Hatuey Fría", category = "Bebidas", priceCup = 350.0, stock = 24, description = "Con o sin leche condensada"),
                    MenuItem(name = "Agua Mineral Embotellada (500ml)", category = "Bebidas", priceCup = 250.0, stock = 50, description = "Agua purificada nacional"),
                    MenuItem(name = "Café Expreso Cubano", category = "Bebidas", priceCup = 150.0, stock = 100, description = "Negro, fuerte y dulce como manda la tradición"),

                    // Coctelería
                    MenuItem(name = "Mojito Clásico Cubano", category = "Coctelería", priceCup = 650.0, stock = 50, description = "Hierbabuena fresca, azúcar, limón, Havana Club 3 años y soda"),
                    MenuItem(name = "Daiquirí Floridita", category = "Coctelería", priceCup = 600.0, stock = 40, description = "Frappeado con ron, limón y marrasquino"),
                    MenuItem(name = "Cuba Libre", category = "Coctelería", priceCup = 550.0, stock = 45, description = "Ron añejo con refresco de cola y rodaja de limón"),
                    MenuItem(name = "Trago Havana Club 7 Años", category = "Coctelería", priceCup = 700.0, stock = 30, description = "En vaso corto a las rocas"),

                    // Postres
                    MenuItem(name = "Flan de Caramelo Casero", category = "Postres", priceCup = 450.0, stock = 15, description = "Cremoso tradicional de leche con caramelo oscuro"),
                    MenuItem(name = "Casquitos de Guayaba con Queso", category = "Postres", priceCup = 480.0, stock = 12, description = "Dulce en almíbar acompañado de lasca de queso blanco")
                )
                dao.insertMenuItems(initialMenuItems)
            }
        }
    }
}
