package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val listarProductos: ListarProductosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoUiState.Fase.Cargando) }

            // Solución errores 1, 2 y 3: Desempaquetar el Result de Kotlin
            val resultado = listarProductos()

            resultado.onSuccess { lista ->
                _uiState.update { estado ->
                    estado.copy(
                        fase = if (lista.isEmpty()) {
                            ProductoUiState.Fase.SinProductos
                        } else {
                            ProductoUiState.Fase.ConProductos(lista.map { p -> p.toUi() })
                        }
                    )
                }
            }.onFailure { excepcion ->
                _uiState.update { estado ->
                    estado.copy(fase = ProductoUiState.Fase.Error(excepcion.message ?: "Error al cargar productos"))
                }
            }
        }
    }

    fun onNombreChange(nuevoNombre: String) {
        _uiState.update { estado ->
            estado.copy(
                formulario = estado.formulario.copy(
                    nombre = nuevoNombre,
                    nombreError = null
                )
            )
        }
    }

    fun onPrecioChange(nuevoPrecio: String) {
        _uiState.update { estado ->
            estado.copy(
                formulario = estado.formulario.copy(
                    precio = nuevoPrecio,
                    precioError = null
                )
            )
        }
    }

    fun onStockChange(nuevoStock: String) {
        _uiState.update { estado ->
            estado.copy(
                formulario = estado.formulario.copy(
                    stock = nuevoStock,
                    stockError = null
                )
            )
        }
    }

    fun registrar() {
        val form = _uiState.value.formulario

        val nombreValido = form.nombre.isNotBlank()
        val precioDouble = form.precio.toDoubleOrNull()
        val precioValido = precioDouble != null && precioDouble > 0.0
        val stockInt = form.stock.toIntOrNull()
        val stockValido = stockInt != null && stockInt >= 0

        if (!nombreValido || !precioValido || !stockValido) {
            _uiState.update { estado ->
                estado.copy(
                    formulario = estado.formulario.copy(
                        nombreError = if (!nombreValido) "El nombre es obligatorio" else null,
                        precioError = if (!precioValido) "Precio inválido" else null,
                        stockError = if (!stockValido) "Stock inválido" else null
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(registrando = true) }
            try {
                // Solución errores 4 y 5: Pasar los parámetros estrictamente como String (.trim())
                registrarProducto(
                    nombre = form.nombre.trim(),
                    precio = form.precio.trim(),
                    stock = form.stock.trim()
                )

                _uiState.update {
                    it.copy(
                        registrando = false,
                        formulario = FormularioProducto(),
                        mensajeExito = "Producto registrado con éxito"
                    )
                }
                cargarProductos()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        registrando = false,
                        formulario = it.formulario.copy(nombreError = e.message ?: "Error al registrar")
                    )
                }
            }
        }
    }
}