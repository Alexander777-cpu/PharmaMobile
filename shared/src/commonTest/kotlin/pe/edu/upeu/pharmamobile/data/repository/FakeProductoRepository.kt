package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class FakeProductoRepository(
    productosIniciales: List<Producto> = emptyList()
) : ProductoRepository {

    private val productos = productosIniciales.toMutableList()
    var simularFalloListado: Boolean = false
    var simularErrorApi: Throwable? = null

    override suspend fun listar(): List<Producto> {
        if (simularFalloListado) throw Exception("Error forzado al listar")
        simularErrorApi?.let { throw it }
        return productos.toList()
    }

    override suspend fun obtener(id: Long): Producto {
        return productos.find { it.id == id } ?: throw Exception("Producto no encontrado")
    }

    override suspend fun registrar(producto: Producto): Producto {
        simularErrorApi?.let { throw it }
        val nuevo = producto.copy(id = (productos.maxOfOrNull { it.id } ?: 0) + 1)
        productos.add(nuevo)
        return nuevo
    }

    override suspend fun actualizar(producto: Producto): Producto {
        simularErrorApi?.let { throw it }
        val index = productos.indexOfFirst { it.id == producto.id }
        if (index != -1) {
            productos[index] = producto
            return producto
        } else {
            throw Exception("Producto no encontrado para actualizar")
        }
    }

    override suspend fun eliminar(id: Long) {
        simularErrorApi?.let { throw it }
        val removido = productos.removeAll { it.id == id }
        if (!removido) throw Exception("Producto no encontrado para eliminar")
    }
}