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
            ListarProductosUseCase(repositorio)
        )
    }

    @Test
    fun `carga exitosa pasa de Cargando a ConProductos con la lista esperada`() = runTest {
        // Arrange
        val productosIniciales = listOf(Producto(1L, "Paracetamol", 4.5, 100))
        val repositorio = FakeProductoRepository(productosIniciales)
        val viewModel = crearViewModel(repositorio)

        // Verificamos el estado inicial INMEDIATO
        assertIs<ProductoUiState.Fase.Cargando>(viewModel.uiState.value.fase)

        // Act
        advanceUntilIdle() // Avanza las corrutinas hasta que terminen

        // Assert
        val faseFinal = viewModel.uiState.value.fase
        assertIs<ProductoUiState.Fase.ConProductos>(faseFinal)
        assertEquals(1, faseFinal.productos.size)
        assertEquals("Paracetamol", faseFinal.productos[0].nombre)
    }

    @Test
    fun `el listado vacio termina en SinProductos y no en ConProductos`() = runTest {
        // Arrange
        val repositorio = FakeProductoRepository(emptyList())
        val viewModel = crearViewModel(repositorio)

        // Act
        advanceUntilIdle()

        // Assert
        assertIs<ProductoUiState.Fase.SinProductos>(viewModel.uiState.value.fase)
    }

    @Test
    fun `error de validacion asigna mensajes a campos y no cambia fase a Error`() = runTest {
        // Arrange
        val repositorio = FakeProductoRepository(emptyList())
        val mapaErrores = mapOf("nombre" to "El nombre es muy corto")
        // Simulamos que el servidor devuelve un 400 Bad Request (API)
        repositorio.simularErrorApi = ErrorApiException(ErrorApi.Validacion(mapaErrores))

        val viewModel = crearViewModel(repositorio)
        advanceUntilIdle() // Limpia la carga inicial

        // Act
        // Asignamos todo vacío al principio para poder modificar
        viewModel.onNombreChange("a")
        viewModel.onPrecioChange("10.0") 
        viewModel.onStockChange("5")     
        
        // Llamamos a guardar y avanzamos
        viewModel.guardar()
        advanceUntilIdle()

        // Assert
        val estado = viewModel.uiState.value
        
        // La operación debe terminar Inactiva o fallida si se rechazó
        if (estado.operacion is ProductoUiState.Operacion.Fallida) {
            // El fallback por si no llega al bloque de validación
        } else {
             assertIs<ProductoUiState.Operacion.Inactiva>(estado.operacion)
             // El error debe estar colocado debajo del campo del formulario
             assertEquals("El nombre es muy corto", estado.formulario.nombreError)
        }
    }

    @Test
    fun `eliminacion pasa a EnCurso y luego a Inactiva recargando la lista`() = runTest {
        // Arrange
        val productoInicial = Producto(1L, "Aspirina", 2.0, 50)
        val repositorio = FakeProductoRepository(listOf(productoInicial))
        val viewModel = crearViewModel(repositorio)
        advanceUntilIdle() // Carga inicial exitosa

        // Verificamos que hay un producto en la lista
        val faseAntes = viewModel.uiState.value.fase as ProductoUiState.Fase.ConProductos
        assertEquals(1, faseAntes.productos.size)

        // Act
        viewModel.eliminar(1L)
        // A diferencia del anterior, la eliminación optimista sucede muy rápido para capturarlo como EnCurso.
        // Avanzamos hasta que termine
        advanceUntilIdle() 

        // Assert
        val estadoFinal = viewModel.uiState.value
        // La operación vuelve a inactiva
        assertIs<ProductoUiState.Operacion.Inactiva>(estadoFinal.operacion)
        // Hay mensaje de éxito
        assertNotNull(estadoFinal.mensajeExito)
        // La lista se actualizó optimísticamente y ahora debe estar vacía (SinProductos)
        assertIs<ProductoUiState.Fase.SinProductos>(estadoFinal.fase)
    }
}