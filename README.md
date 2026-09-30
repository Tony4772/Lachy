# Paladar Lachy • POS Restaurante (Cuba)

Aplicación Android desarrollada en **Kotlin** y **Jetpack Compose** para la gestión integral de paladares y restaurantes en Cuba (Cienfuegos). Diseñada con arquitectura **100% Offline-First**, cálculo multimoneda dinámico (CUP, USD, MLC, EUR), control de inventario/stock crítico y reporte de cierre de caja para WhatsApp.

---

## 🛠️ Requisitos para abrir en Android Studio

1. **Android Studio**: Versión recomendada Hedgehog, Iguana, Jellyfish, Koala, Ladybug o superior.
2. **JDK**: JDK 17 o JDK 21 (incluido por defecto en Android Studio bajo *Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK*).
3. **Android SDK**:
   - `compileSdk`: 36
   - `minSdk`: 24 (Android 7.0+)
   - `targetSdk`: 36

---

## 🚀 Flujo con GitHub & GitHub Desktop

1. **Clonar con GitHub Desktop**:
   - En GitHub Desktop: `File > Clone Repository...`
   - Selecciona el repositorio y clónalo en tu carpeta local deseada.
2. **Abrir en Android Studio**:
   - Abre Android Studio y selecciona **Open** (o `File > Open...`).
   - Navega hasta la carpeta del repositorio clonado y selecciónala.
   - Android Studio detectará automáticamente el archivo `settings.gradle.kts` y el `gradlew` (Gradle Wrapper).
   - Espera a que termine la sincronización de Gradle (`Gradle Sync`).
3. **Ejecutar la app**:
   - Conecta un dispositivo Android por USB o crea un emulador en el *Device Manager*.
   - Presiona el botón verde de **Run** (`Shift + F10`).
   - O compila el APK por terminal:
     ```bash
     ./gradlew assembleDebug
     ```
     (En Windows: `gradlew.bat assembleDebug`)

---

## 📦 Estructura del Proyecto

- `app/src/main/java/com/example/`:
  - `data/model/`: Entidades Room (`MenuItem`, `TableEntity`, `OrderEntity`, `OrderItem`, `ExchangeRate`, `Expense`).
  - `data/dao/`: `RestaurantDao` con queries reactivas en `Flow`.
  - `data/database/`: `RestaurantDatabase` con precarga inicial de platos criollos y mesas de Cienfuegos.
  - `data/repository/`: Lógica de negocio y persistencia.
  - `ui/screens/`: Pantallas de Mesas, Comandas (POS), Inventario, Arqueo de Caja y Tasas de Cambio.
  - `ui/components/`: Diálogo de cobro multimoneda y ticket compartible.
  - `ui/utils/`: Formateador de divisas y generador de reportes de texto para WhatsApp/SMS.
