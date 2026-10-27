package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobile.domain.model.Producto

class ProductoLocal(db: PharmaDb) {
    private val consultas = db.productoQueries

    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun pendientes(): List<Producto> =
        consultas.listarPendientes().executeAsList().map { it.aDominio() }

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val nuevoIdLocal = minOf(menor, 0L) - 1L
        val entidad = pe.edu.upeu.pharmamobil.db.Producto(
            id = nuevoIdLocal,
            nombre = producto.nombre,
            precio = producto.precio,
            stock = producto.stock.toLong(),
            pendiente = 1L
        )
        consultas.insertar(entidad.id, entidad.nombre, entidad.precio, entidad.stock, entidad.pendiente)
        return producto.copy(id = nuevoIdLocal)
    }

    fun borrarPorId(id: Long) {
        consultas.borrarPorId(id)
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach { p ->
                consultas.insertar(
                    id = p.id,
                    nombre = p.nombre,
                    precio = p.precio,
                    stock = p.stock.toLong(),
                    pendiente = 0L
                )
            }
        }
    }
}

fun pe.edu.upeu.pharmamobil.db.Producto.aDominio(): Producto = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock.toInt()
)
