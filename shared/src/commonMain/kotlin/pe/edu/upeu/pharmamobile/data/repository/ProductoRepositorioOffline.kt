package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoRepositorioRest
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun listar(): List<Producto> = local.pendientes() + (runCatching { remoto.listar() }.getOrDefault(emptyList()))

    override suspend fun obtener(id: Long): Producto = remoto.obtener(id)

    override suspend fun registrar(producto: Producto): Producto {
        val guardadoLocal = local.guardarPendiente(producto)
        runCatching { sincronizar() }
        return guardadoLocal
    }

    override suspend fun actualizar(producto: Producto): Producto {
        return remoto.actualizar(producto)
    }

    override suspend fun eliminar(id: Long) {
        remoto.eliminar(id)
    }

    override suspend fun sincronizar() {
        val pendientes = local.pendientes()
        pendientes.forEach { p ->
            runCatching {
                remoto.registrar(p)
                local.borrarPorId(p.id)
            }.onFailure { e ->
                val error = (e as? ErrorApiException)?.error
                if (error is ErrorApi.SesionExpirada) {
                    throw e
                }
            }
        }

        try {
            val remotos = remoto.listar()
            local.reemplazarSincronizados(remotos)
        } catch (e: Exception) {
            val error = (e as? ErrorApiException)?.error
            if (error is ErrorApi.SesionExpirada) {
                throw e
            } else {
                throw ErrorApiException(ErrorApi.SinConexion)
            }
        }
    }
}
