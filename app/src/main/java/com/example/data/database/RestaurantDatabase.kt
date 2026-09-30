package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.RestaurantDao
import com.example.data.model.ExchangeRate
import com.example.data.model.MovementType
import com.example.data.model.PurchaseRecord
import com.example.data.model.PurchaseStatus
import com.example.data.model.StockItem
import com.example.data.model.StockMovement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StockItem::class,
        PurchaseRecord::class,
        StockMovement::class,
        ExchangeRate::class
    ],
    version = 3,
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
                    "control_lachy_cienfuegos.db"
                )
                    .fallbackToDestructiveMigration()
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
                // 1. Tasas de cambio (Cuba)
                val initialRates = listOf(
                    ExchangeRate(currencyCode = "USD", rateToCup = 330.0, symbol = "$"),
                    ExchangeRate(currencyCode = "MLC", rateToCup = 270.0, symbol = "MLC"),
                    ExchangeRate(currencyCode = "EUR", rateToCup = 345.0, symbol = "€")
                )
                dao.insertExchangeRates(initialRates)

                // 2. Inventario inicial de Almacén
                val initialStockItems = listOf(
                    StockItem(
                        name = "Queso Barra / Gouda",
                        category = "Insumos",
                        unit = "Libras",
                        currentStock = 12.0,
                        minStockAlert = 25.0,
                        lastUnitPriceCup = 680.0,
                        notes = "Insumo crítico en almacén"
                    ),
                    StockItem(
                        name = "Harina de Trigo Especial",
                        category = "Insumos",
                        unit = "Libras",
                        currentStock = 85.0,
                        minStockAlert = 40.0,
                        lastUnitPriceCup = 220.0,
                        notes = "Sacos en almacén central"
                    ),
                    StockItem(
                        name = "Jamón Barra",
                        category = "Insumos",
                        unit = "Libras",
                        currentStock = 8.0,
                        minStockAlert = 15.0,
                        lastUnitPriceCup = 750.0,
                        notes = "Jamón en refrigeración"
                    ),
                    StockItem(
                        name = "Puré de Tomate / Salsa",
                        category = "Insumos",
                        unit = "Latas",
                        currentStock = 18.0,
                        minStockAlert = 12.0,
                        lastUnitPriceCup = 380.0,
                        notes = "Latas grandes"
                    ),
                    StockItem(
                        name = "Aceite Vegetal",
                        category = "Insumos",
                        unit = "Litros",
                        currentStock = 6.0,
                        minStockAlert = 5.0,
                        lastUnitPriceCup = 800.0,
                        notes = "Bidones de aceite"
                    ),
                    StockItem(
                        name = "Levadura Seca",
                        category = "Insumos",
                        unit = "Libras",
                        currentStock = 4.0,
                        minStockAlert = 3.0,
                        lastUnitPriceCup = 450.0,
                        notes = "Levadura para panadería y masas"
                    ),
                    StockItem(
                        name = "Moldes de Pizza 30cm",
                        category = "Equipos y Utensilios",
                        unit = "Moldes",
                        currentStock = 24.0,
                        minStockAlert = 20.0,
                        lastUnitPriceCup = 1200.0,
                        notes = "Moldes metálicos grandes en almacén"
                    ),
                    StockItem(
                        name = "Moldes de Pizza 20cm",
                        category = "Equipos y Utensilios",
                        unit = "Moldes",
                        currentStock = 32.0,
                        minStockAlert = 25.0,
                        lastUnitPriceCup = 850.0,
                        notes = "Moldes individuales"
                    ),
                    StockItem(
                        name = "Cajas Termopack",
                        category = "Empaques",
                        unit = "Termopacks",
                        currentStock = 20.0,
                        minStockAlert = 60.0,
                        lastUnitPriceCup = 45.0,
                        notes = "Cajas térmicas para entregas"
                    ),
                    StockItem(
                        name = "Bolsas de Papel / Empaque",
                        category = "Empaques",
                        unit = "Unidades",
                        currentStock = 110.0,
                        minStockAlert = 50.0,
                        lastUnitPriceCup = 20.0,
                        notes = "Bolsas de despacho"
                    )
                )
                dao.insertStockItems(initialStockItems)

                // 3. Compras registradas por el comprador
                val initialPurchases = listOf(
                    PurchaseRecord(
                        itemName = "Queso Barra / Gouda",
                        quantity = 25.0,
                        unit = "Libras",
                        unitPriceCup = 670.0,
                        totalCostCup = 16750.0,
                        buyerName = "Comprador",
                        purchasePlace = "Mercado Agro Cienfuegos",
                        status = PurchaseStatus.COMPRADO_EN_CAMINO,
                        requestedAt = System.currentTimeMillis() - 3600000,
                        purchasedAt = System.currentTimeMillis() - 1800000,
                        notes = "Comprado hace 30 min. Va en camino al almacén."
                    ),
                    PurchaseRecord(
                        itemName = "Cajas Termopack",
                        quantity = 100.0,
                        unit = "Termopacks",
                        unitPriceCup = 45.0,
                        totalCostCup = 4500.0,
                        buyerName = "Comprador",
                        purchasePlace = "Distribuidor particular",
                        status = PurchaseStatus.NECESITA_COMPRAR,
                        requestedAt = System.currentTimeMillis() - 7200000,
                        notes = "Falta comprar: Quedan pocos termopacks en almacén."
                    ),
                    PurchaseRecord(
                        itemName = "Harina de Trigo Especial",
                        quantity = 50.0,
                        unit = "Libras",
                        unitPriceCup = 210.0,
                        totalCostCup = 10500.0,
                        buyerName = "Comprador",
                        purchasePlace = "Almacén Mayorista Cienfuegos",
                        status = PurchaseStatus.RECIBIDO_EN_ALMACEN,
                        requestedAt = System.currentTimeMillis() - 86400000,
                        purchasedAt = System.currentTimeMillis() - 82800000,
                        receivedAt = System.currentTimeMillis() - 79200000,
                        receivedBy = "Encargado de Almacén",
                        notes = "Saco de harina sellado, ingresado al stock."
                    )
                )
                dao.insertPurchases(initialPurchases)

                // 4. Movimientos iniciales de almacén
                val initialMovements = listOf(
                    StockMovement(
                        stockItemId = 2L,
                        itemName = "Harina de Trigo Especial",
                        type = MovementType.ENTRADA_COMPRA,
                        quantity = 50.0,
                        unit = "Libras",
                        timestamp = System.currentTimeMillis() - 79200000,
                        registeredBy = "Encargado de Almacén",
                        notes = "Entrada por compra recibida"
                    ),
                    StockMovement(
                        stockItemId = 1L,
                        itemName = "Queso Barra / Gouda",
                        type = MovementType.SALIDA_DESPACHO,
                        quantity = -10.0,
                        unit = "Libras",
                        timestamp = System.currentTimeMillis() - 43200000,
                        registeredBy = "Encargado de Almacén",
                        notes = "Despacho para producción"
                    )
                )
                initialMovements.forEach { dao.insertMovement(it) }
            }
        }
    }
}
