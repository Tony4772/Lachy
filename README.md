# Control Lachy • Control de Inventarios & Almacén

Aplicación Android desarrollada en **Kotlin** y **Jetpack Compose** diseñada para el control total de inventarios, compras y almacén. Resuelve la coordinación en tiempo real entre el **Comprador** (quien gestiona compras, precios por libra/unidad e ingresos) y el **Dueño** (quien supervisa existencias, entradas, salidas y valor del almacén desde cualquier lugar).

---

## 🎯 Funcionalidades Principales

1. **📊 Panel del Dueño (Supervisión Total):**
   - **Valor monetario total del inventario** en almacén (en CUP y equivalente en USD).
   - Conteo de productos disponibles, compras en camino y alertas de stock bajo/agotado.
   - Resumen de lo que entra y lo que sale de almacén.
   - Botón de 1 toque para compartir el reporte de inventario completo por **WhatsApp** o mensaje.

2. **📦 Inventario & Almacén:**
   - Catálogo de productos con buscador instantáneo y filtros por categorías (Insumos, Empaques, Equipos, Bebidas, etc.).
   - Existencias en tiempo real, unidad de medida (Libras, Moldes, Unidades, Cajas, etc.) y precio unitario de referencia.
   - Acciones directas por producto:
     - **+ Entrada Rápida**: Sumar existencias indicando precio pagado y proveedor.
     - **- Salida Rápida**: Descontar existencias por despacho, consumo interno o merma.
     - **Conteo Físico**: Ajustar el stock real tras inventario físico.
   - Alta y edición de nuevos productos.

3. **🛒 Comprador & Entradas:**
   - Lista prioritaria de productos con **Stock Bajo / Por Comprar**.
   - Registro de compras en la calle o agro:
     - Producto, cantidad, precio unitario pagado (ej. precio por libra de queso).
     - Cálculo automático del costo total.
     - Proveedor o lugar de compra.
     - Opción de registrar como "En Camino" o "Ingresado directamente al almacén".
   - Confirmación de llegada de compras en tránsito al almacén con un solo toque.

4. **📋 Salidas & Kárdex (Historial):**
   - Registro de salidas de almacén indicando motivo (despacho, consumo, merma) y persona responsable.
   - Historial cronológico con filtros (Todas, Entradas, Salidas, Ajustes).

---

## 🚀 Flujo con GitHub & Android Studio

1. Clonar el repositorio con **GitHub Desktop**.
2. Abrir la carpeta clonada en **Android Studio** (`File > Open...`).
3. El proyecto incluye `gradlew`, `gradlew.bat` y `gradle-wrapper.jar` pre-configurados.
4. Compilar APK con:
   ```bash
   ./gradlew assembleDebug
   ```
