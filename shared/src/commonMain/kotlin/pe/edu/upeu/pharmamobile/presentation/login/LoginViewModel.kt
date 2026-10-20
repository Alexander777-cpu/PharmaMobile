package pe.edu.upeu.pharmamobile.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.data.repository.AuthException
import pe.edu.upeu.pharmamobile.domain.error.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        verificarSesionGuardada()
        observarExpiracion()
    }

    private fun observarExpiracion() {
        viewModelScope.launch {
            authRepository.mensajeExpiracion.collect { mensaje ->
                if (mensaje != null) {
                    _uiState.update { it.copy(sesion = null, error = mensaje) }
                }
            }
        }
    }

    fun onUsuarioChange(nuevoUsuario: String) {
        authRepository.limpiarMensajeExpiracion()
        _uiState.update { it.copy(usuario = nuevoUsuario, error = null) }
    }

    fun onClaveChange(nuevaClave: String) {
        authRepository.limpiarMensajeExpiracion()
        _uiState.update { it.copy(clave = nuevaClave, error = null) }
    }

    fun verificarSesionGuardada() {
        viewModelScope.launch {
            val sesion = authRepository.sesionGuardada()
            if (sesion != null) {
                _uiState.update { it.copy(sesion = sesion) }
            }
        }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val sesion = authRepository.iniciarSesion(
                    Credenciales(estado.usuario, estado.clave)
                )
                _uiState.update { it.copy(cargando = false, sesion = sesion) }
            } catch (e: AuthException) {
                val mensajeError = when (val err = e.errorSesion) {
                    ErrorSesion.CredencialesInvalidas -> "Usuario o contraseña incorrectos."
                    is ErrorSesion.SesionExpirada -> {
                        val msg = err.mensaje
                        when {
                            msg?.contains("expirado", ignoreCase = true) == true -> "El token ha expirado. Inicie sesión nuevamente"
                            msg?.contains("deshabilitado", ignoreCase = true) == true -> "El usuario está deshabilitado"
                            !msg.isNullOrBlank() -> msg
                            else -> "El token ha expirado. Inicie sesión nuevamente"
                        }
                    }
                    is ErrorSesion.UsuarioDeshabilitado -> "El usuario está deshabilitado"
                    is ErrorSesion.MensajeCustom -> err.mensaje
                    ErrorSesion.SinPermiso -> "No cuenta con los permisos necesarios."
                    ErrorSesion.SinConexion -> "Error de conexión con el servidor."
                }
                _uiState.update { it.copy(cargando = false, error = mensajeError) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "Error al iniciar sesión: ${e.message}") }
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.cerrarSesion()
            _uiState.update { SesionUiState() }
        }
    }
}
