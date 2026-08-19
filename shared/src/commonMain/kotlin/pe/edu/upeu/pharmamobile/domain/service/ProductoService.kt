package pe.edu.upeu.pharmamobile.domain.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

class ProductoService {

    // Pasos 2 & 3: Fuente temporal de productos simulados
    private val productosSimulados = listOf(
        Producto(1L, "Paracetamol", 8.50, 100),
        Producto(2L, "Ibuprofeno", 12.00, 50),
        Producto(3L, "Amoxicilina", 18.50, 20)
    )

    // Pasos 4 & 5: Operación asíncrona puntual con suspend y delay
    suspend fun obtenerProductos(): List<Producto> {
        delay(1000) // Simulación de espera de red
        return productosSimulados
    }

    // Pasos 9 a 11: Flujo básico de emisión secuencial
    fun observarEstados(): Flow<String> = flow {
        emit("Iniciando")
        delay(1000)
        emit("Finalizado")
    }

    // Pasos 12 a 14: Flujo de productos aplicando copy() para simular cambio de stock
    fun observarProductos(): Flow<List<Producto>> = flow {
        emit(emptyList()) // Emisión inicial vacía
        delay(1000)
        // Simulación de actualización dinámica con copy()
        val productosActualizados = productosSimulados.map {
            if (it.id == 1L) it.copy(stock = 90) else it
        }
        emit(productosActualizados)
    }

    // Pasos 15 a 17: Flujo integrado de estados con Sealed Class (Cargando -> Éxito)
    fun cargarProductos(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)
        delay(1000)
        emit(ResultadoProductos.Exito(productosSimulados))
    }
}