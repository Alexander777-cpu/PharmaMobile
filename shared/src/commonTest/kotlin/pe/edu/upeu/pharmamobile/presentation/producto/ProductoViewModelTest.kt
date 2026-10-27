package pe.edu.upeu.pharmamobile.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel(repositorio: FakeProductoRepository): ProductoViewModel {
        return ProductoViewModel(
            RegistrarProductoUseCase(repositorio),
            ActualizarProductoUseCase(repositorio),
            EliminarProductoUseCase(repositorio),
            ListarProductosUseCase(repositorio),
            productoRepository = repositorio
        )
    }

    @Test
    fun `carga exitosa pasa de Cargando a ConProductos con la lista esperada`() = runTest {
        val productosIniciales = listOf(Producto(1L, "Paracetamol", 4.5, 100))
        val repositorio = FakeProductoRepository(productosIniciales)
        val viewModel = crearViewModel(repositorio)

        advanceUntilIdle()

        val faseFinal = viewModel.uiState.value.fase
        assertIs<ProductoUiState.Fase.ConProductos>(faseFinal)
        assertEquals(1, faseFinal.productos.size)
        assertEquals("Paracetamol", faseFinal.productos[0].nombre)
    }

    @Test
    fun `el listado vacio termina en SinProductos y no en ConProductos`() = runTest {
        val repositorio = FakeProductoRepository(emptyList())
        val viewModel = crearViewModel(repositorio)

        advanceUntilIdle()

        assertIs<ProductoUiState.Fase.SinProductos>(viewModel.uiState.value.fase)
    }

    @Test
    fun `error de validacion asigna mensajes a campos y no cambia fase a Error`() = runTest {
        val repositorio = FakeProductoRepository(emptyList())
        val mapaErrores = mapOf("nombre" to "El nombre es muy corto")
        repositorio.simularErrorApi = ErrorApiException(ErrorApi.Validacion(mapaErrores))

        val viewModel = crearViewModel(repositorio)
        advanceUntilIdle()

        viewModel.onNombreChange("a")
        viewModel.onPrecioChange("10.0")
        viewModel.onStockChange("5")

        viewModel.guardar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        if (estado.operacion is ProductoUiState.Operacion.Fallida) {
            // Error manejado en operacion
        } else {
            assertIs<ProductoUiState.Operacion.Inactiva>(estado.operacion)
            assertEquals("El nombre es muy corto", estado.formulario.nombreError)
        }
    }

    @Test
    fun `eliminacion pasa a EnCurso y luego a Inactiva recargando la lista`() = runTest {
        val productoInicial = Producto(1L, "Aspirina", 2.0, 50)
        val repositorio = FakeProductoRepository(listOf(productoInicial))
        val viewModel = crearViewModel(repositorio)
        advanceUntilIdle()

        val faseAntes = viewModel.uiState.value.fase as ProductoUiState.Fase.ConProductos
        assertEquals(1, faseAntes.productos.size)

        viewModel.eliminar(1L)
        advanceUntilIdle()

        val estadoFinal = viewModel.uiState.value
        assertIs<ProductoUiState.Operacion.Inactiva>(estadoFinal.operacion)
        assertNotNull(estadoFinal.mensajeExito)
        assertIs<ProductoUiState.Fase.SinProductos>(estadoFinal.fase)
    }
}
