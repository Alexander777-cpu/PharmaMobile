package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.model.Producto

interface ProductoRepository {
    fun observar(): Flow<List<Producto>>
    suspend fun listar(): List<Producto>
    suspend fun obtener(id: Long): Producto
    suspend fun registrar(producto: Producto): Producto
    suspend fun actualizar(producto: Producto): Producto
    suspend fun eliminar(id: Long)
    suspend fun sincronizar()
}
