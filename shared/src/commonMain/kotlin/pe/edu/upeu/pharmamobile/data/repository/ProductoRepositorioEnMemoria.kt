package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria : ProductoRepository {

    private val candado = Mutex()
    private val productos = mutableListOf<Producto>()
    private var siguienteId = 1L

    override fun observar(): Flow<List<Producto>> = flowOf(productos.toList())

    override suspend fun sincronizar() {}

    override suspend fun registrar(producto: Producto): Producto {
        delay(RETARDO_REGISTRO_MS)
        return candado.withLock {
            val guardado = producto.copy(id = siguienteId++)
            productos.add(guardado)
            guardado
        }
    }

    override suspend fun listar(): List<Producto> {
        delay(RETARDO_LISTADO_MS)
        return candado.withLock {
            productos.toList()
        }
    }

    override suspend fun obtener(id: Long): Producto {
        delay(RETARDO_LISTADO_MS)
        return candado.withLock {
            productos.find { it.id == id }
                ?: throw Exception("Producto no encontrado en memoria")
        }
    }

    override suspend fun actualizar(producto: Producto): Producto {
        delay(RETARDO_REGISTRO_MS)
        return candado.withLock {
            val index = productos.indexOfFirst { it.id == producto.id }
            if (index != -1) {
                productos[index] = producto
                producto
            } else {
                throw Exception("Producto no encontrado para actualizar")
            }
        }
    }

    override suspend fun eliminar(id: Long) {
        delay(RETARDO_REGISTRO_MS)
        candado.withLock {
            val removido = productos.removeAll { it.id == id }
            if (!removido) {
                throw Exception("Producto no encontrado para eliminar")
            }
        }
    }

    private companion object {
        const val RETARDO_REGISTRO_MS = 400L
        const val RETARDO_LISTADO_MS = 600L
    }
}
