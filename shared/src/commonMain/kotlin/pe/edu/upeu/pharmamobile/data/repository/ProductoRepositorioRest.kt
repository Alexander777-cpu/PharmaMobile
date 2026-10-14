package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.mapper.toRequest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioRest(
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long
) : ProductoRepository {

    override suspend fun listar(): List<Producto> = ejecutarLlamada {
        api.listar()
            .contenido
            .filter { it.estado == true }
            .map { it.toDomain() }
    }

    override suspend fun obtener(id: Long): Producto = ejecutarLlamada {
        api.obtener(id).toDomain()
    }

    override suspend fun registrar(producto: Producto): Producto = ejecutarLlamada {
        api.crear(producto.toRequest(categoriaPorDefecto))
            .toDomain()
    }

    override suspend fun actualizar(producto: Producto): Producto = ejecutarLlamada {
        api.actualizar(producto.id, producto.toRequest(categoriaPorDefecto))
            .toDomain()
    }

    override suspend fun eliminar(id: Long) = ejecutarLlamada {
        api.eliminar(id)
    }
}