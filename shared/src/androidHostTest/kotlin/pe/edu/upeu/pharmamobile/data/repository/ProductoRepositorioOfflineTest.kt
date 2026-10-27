package pe.edu.upeu.pharmamobile.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoRepositorioOfflineTest {

    private fun baseEnMemoria(): PharmaDb {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PharmaDb.Schema.create(driver)
        return PharmaDb(driver)
    }

    @Test
    fun `guardarPendiente asigna un id negativo y marca el registro como pendiente`() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)
        val productoNuevo = Producto(0L, "Ibuprofeno 400mg", 5.5, 20)

        val guardado = local.guardarPendiente(productoNuevo)

        assertTrue(guardado.id < 0L, "El id debe ser negativo")
        val pendientes = local.pendientes()
        assertEquals(1, pendientes.size)
        assertEquals(guardado.id, pendientes.first().id)
    }

    @Test
    fun `siguienteIdLocal decrementa correctamente con multiples registros locales`() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)

        val p1 = local.guardarPendiente(Producto(0L, "Producto 1", 10.0, 5))
        val p2 = local.guardarPendiente(Producto(0L, "Producto 2", 15.0, 10))
        val p3 = local.guardarPendiente(Producto(0L, "Producto 3", 20.0, 15))

        assertEquals(-1L, p1.id)
        assertEquals(-2L, p2.id)
        assertEquals(-3L, p3.id)
    }

    @Test
    fun `observar emite la lista de productos de la base de datos local`() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)

        local.guardarPendiente(Producto(0L, "Paracetamol", 4.0, 50))
        val listaEmitida = local.observar().first()

        assertEquals(1, listaEmitida.size)
        assertEquals("Paracetamol", listaEmitida.first().nombre)
    }

    @Test
    fun `reemplazarSincronizados borra los productos con pendiente 0 y guarda los remotos`() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)

        local.reemplazarSincronizados(listOf(Producto(100L, "Aspirina Vieja", 2.0, 10)))
        assertEquals(1, local.observar().first().size)

        val remotosNuevos = listOf(
            Producto(200L, "Aspirina Nueva", 2.5, 15),
            Producto(201L, "Omeprazol", 8.0, 30)
        )
        local.reemplazarSincronizados(remotosNuevos)

        val listaFinal = local.observar().first()
        assertEquals(2, listaFinal.size)
        assertEquals("Aspirina Nueva", listaFinal[0].nombre)
        assertEquals("Omeprazol", listaFinal[1].nombre)
    }

    @Test
    fun `sincronizar sube pendientes antes de descargar de la red`() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)

        val registradoEnServidor = mutableListOf<Producto>()
        val repositorioRestFake = object : ProductoRepository {
            override fun observar() = error("No usado")
            override suspend fun listar(): List<Producto> = registradoEnServidor.toList()
            override suspend fun obtener(id: Long) = error("No usado")
            override suspend fun registrar(producto: Producto): Producto {
                val conIdRemoto = producto.copy(id = 500L)
                registradoEnServidor.add(conIdRemoto)
                return conIdRemoto
            }
            override suspend fun actualizar(producto: Producto) = error("No usado")
            override suspend fun eliminar(id: Long) {}
            override suspend fun sincronizar() {}
        }

        val repositorioOffline = ProductoRepositorioOffline(
            local = local,
            remoto = ProductoRepositorioRestFakeAdapter(repositorioRestFake)
        )

        local.guardarPendiente(Producto(0L, "Amoxicilina", 12.0, 8))
        assertEquals(1, local.pendientes().size)

        repositorioOffline.sincronizar()

        assertEquals(1, registradoEnServidor.size)
        assertEquals("Amoxicilina", registradoEnServidor.first().nombre)
        assertEquals(0, local.pendientes().size)
    }

    @Test
    fun `operacion registrar guarda localmente y sincroniza sin lanzar excepcion en caso de error de red`() = runTest {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)

        val repositorioRestFalla = object : ProductoRepository {
            override fun observar() = error("No usado")
            override suspend fun listar(): List<Producto> = throw Exception("Sin red")
            override suspend fun obtener(id: Long) = error("No usado")
            override suspend fun registrar(producto: Producto): Producto = throw Exception("Sin red")
            override suspend fun actualizar(producto: Producto) = error("No usado")
            override suspend fun eliminar(id: Long) {}
            override suspend fun sincronizar() {}
        }

        val repositorioOffline = ProductoRepositorioOffline(
            local = local,
            remoto = ProductoRepositorioRestFakeAdapter(repositorioRestFalla)
        )

        val resultado = repositorioOffline.registrar(Producto(0L, "Vitamina C", 6.0, 100))

        assertTrue(resultado.id < 0L, "El registro local se guardó con id negativo")
        assertEquals(1, local.pendientes().size)
    }
}

private class ProductoRepositorioRestFakeAdapter(
    private val fakeRepo: ProductoRepository
) : pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRest(
    api = pe.edu.upeu.pharmamobile.data.remote.ProductoApi(io.ktor.client.HttpClient()),
    categoriaPorDefecto = 1L
) {
    override suspend fun registrar(producto: Producto): Producto = fakeRepo.registrar(producto)
    override suspend fun listar(): List<Producto> = fakeRepo.listar()
}
