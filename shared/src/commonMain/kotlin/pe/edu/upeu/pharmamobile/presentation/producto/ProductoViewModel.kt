package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.comoTextoParaCompartir

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val actualizarProducto: ActualizarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase,
    private val listarProductos: ListarProductosUseCase,
    private val authRepository: AuthRepository? = null,
    private val compartidor: Compartidor = object : Compartidor { override fun compartir(texto: String) {} }
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoUiState.Fase.Cargando) }

            listarProductos().fold(
                onSuccess = { lista ->
                    _uiState.update { estado ->
                        estado.copy(
                            fase = if (lista.isEmpty()) ProductoUiState.Fase.SinProductos
                            else ProductoUiState.Fase.ConProductos(lista.map { it.toUi() })
                        )
                    }
                },
                onFailure = { excepcion ->
                    val errorDominio = (excepcion as? ErrorApiException)?.error
                    if (errorDominio is ErrorApi.SesionExpirada) {
                        authRepository?.expirarSesion(errorDominio.mensaje)
                    } else {
                        val mensaje = traducirError(excepcion, esCreacion = false)
                        _uiState.update { estado ->
                            estado.copy(fase = ProductoUiState.Fase.Error(mensaje))
                        }
                    }
                }
            )
        }
    }

    fun onNombreChange(nuevoNombre: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(nombre = nuevoNombre, nombreError = null),
                operacion = ProductoUiState.Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    fun onPrecioChange(nuevoPrecio: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(precio = nuevoPrecio, precioError = null),
                operacion = ProductoUiState.Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    fun onStockChange(nuevoStock: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(stock = nuevoStock, stockError = null),
                operacion = ProductoUiState.Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    fun seleccionarParaEdicion(producto: ProductoUi) {
        _uiState.update {
            it.copy(
                idEdicion = producto.id.toLongOrNull(),
                formulario = FormularioProducto(
                    nombre = producto.nombre,
                    precio = producto.precio.replace(",", ".").replace("[^0-9.]".toRegex(), "").trim(),
                    stock = producto.stock.replace("[^0-9]".toRegex(), "").trim()
                ),
                operacion = ProductoUiState.Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    fun cancelarEdicion() {
        _uiState.update {
            it.copy(
                idEdicion = null,
                formulario = FormularioProducto(),
                operacion = ProductoUiState.Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    fun guardar() {
        if (_uiState.value.operacion is ProductoUiState.Operacion.EnCurso) return

        val form = _uiState.value.formulario
        val idEdicion = _uiState.value.idEdicion
        val esCreacion = idEdicion == null
        val tipoOperacion = if (esCreacion) ProductoUiState.Tipo.Crear else ProductoUiState.Tipo.Actualizar

        viewModelScope.launch {
            _uiState.update { it.copy(operacion = ProductoUiState.Operacion.EnCurso(tipoOperacion), mensajeExito = null) }

            val resultado = if (esCreacion) {
                registrarProducto(form.nombre, form.precio, form.stock)
            } else {
                actualizarProducto(idEdicion!!, form.nombre, form.precio, form.stock)
            }

            resultado.fold(
                onSuccess = { productoModificado ->
                    _uiState.update { estado ->
                        val faseActual = estado.fase
                        val nuevaFase = if (faseActual is ProductoUiState.Fase.ConProductos) {
                            if (esCreacion) {
                                ProductoUiState.Fase.ConProductos(listOf(productoModificado.toUi()) + faseActual.productos)
                            } else {
                                ProductoUiState.Fase.ConProductos(faseActual.productos.map { if (it.id == productoModificado.id.toString()) productoModificado.toUi() else it })
                            }
                        } else {
                            if (esCreacion) ProductoUiState.Fase.ConProductos(listOf(productoModificado.toUi())) else faseActual
                        }

                        estado.copy(
                            operacion = ProductoUiState.Operacion.Inactiva,
                            formulario = FormularioProducto(),
                            idEdicion = null,
                            mensajeExito = if (esCreacion) "Producto registrado con éxito" else "Producto actualizado con éxito",
                            fase = nuevaFase
                        )
                    }
                },
                onFailure = { manejarFallo(it, esCreacion) }
            )
        }
    }

    fun eliminar(id: Long) {
        if (_uiState.value.operacion is ProductoUiState.Operacion.EnCurso) return

        viewModelScope.launch {
            _uiState.update { it.copy(operacion = ProductoUiState.Operacion.EnCurso(ProductoUiState.Tipo.Eliminar), mensajeExito = null) }

            eliminarProducto(id).fold(
                onSuccess = {
                    _uiState.update { estado ->
                        val faseActual = estado.fase
                        val nuevaFase = if (faseActual is ProductoUiState.Fase.ConProductos) {
                            val listaFiltrada = faseActual.productos.filter { it.id != id.toString() }
                            if (listaFiltrada.isEmpty()) ProductoUiState.Fase.SinProductos else ProductoUiState.Fase.ConProductos(listaFiltrada)
                        } else faseActual

                        estado.copy(
                            operacion = ProductoUiState.Operacion.Inactiva,
                            mensajeExito = "Producto eliminado",
                            fase = nuevaFase
                        )
                    }
                },
                onFailure = { manejarFallo(it, esCreacion = false) }
            )
        }
    }

    fun compartirProducto(producto: ProductoUi) {
        compartidor.compartir(producto.comoTextoParaCompartir())
    }

    private fun manejarFallo(fallo: Throwable, esCreacion: Boolean) {
        val errorDominio = (fallo as? ErrorApiException)?.error

        if (errorDominio is ErrorApi.SesionExpirada) {
            viewModelScope.launch {
                authRepository?.expirarSesion(errorDominio.mensaje)
            }
            return
        }

        _uiState.update { estado ->
            when (errorDominio) {
                is ErrorApi.Validacion -> {
                    estado.copy(
                        operacion = ProductoUiState.Operacion.Inactiva,
                        formulario = estado.formulario.copy(
                            nombreError = errorDominio.porCampo["nombre"],
                            precioError = errorDominio.porCampo["precio"],
                            stockError = errorDominio.porCampo["stock"]
                        )
                    )
                }
                else -> {
                    if (fallo is ProductoInvalidoException) {
                        estado.copy(
                            operacion = ProductoUiState.Operacion.Inactiva,
                            formulario = estado.formulario.copy(
                                nombreError = fallo.errores.nombre,
                                precioError = fallo.errores.precio,
                                stockError = fallo.errores.stock
                            )
                        )
                    } else {
                        val mensaje = traducirError(fallo, esCreacion)
                        estado.copy(operacion = ProductoUiState.Operacion.Fallida(mensaje))
                    }
                }
            }
        }
    }

    private fun traducirError(fallo: Throwable, esCreacion: Boolean): String {
        val errorDominio = (fallo as? ErrorApiException)?.error
        return when (errorDominio) {
            is ErrorApi.Validacion -> "Revisa los datos ingresados en el formulario."
            ErrorApi.NoEncontrado -> if (esCreacion) "Ruta o categoría no encontrada en el servidor (404)." else "El producto ya no existe."
            is ErrorApi.Conflicto -> errorDominio.mensaje
            is ErrorApi.SesionExpirada -> "El token ha expirado. Inicie sesión nuevamente"
            ErrorApi.SinPermiso -> "No tienes permisos para realizar esta acción."
            ErrorApi.NoAutorizado -> "No tienes permisos para realizar esta acción."
            ErrorApi.Servidor -> "Error interno del servidor (500)."
            ErrorApi.SinConexion -> "Sin conexión con el servidor."
            ErrorApi.TiempoAgotado -> "El servidor tardó demasiado en responder."
            null -> fallo.message ?: "Error al procesar la solicitud."
        }
    }
}
