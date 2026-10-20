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
    }

    fun onUsuarioChange(nuevoUsuario: String) {
        _uiState.update { it.copy(usuario = nuevoUsuario, error = null) }
    }

    fun onClaveChange(nuevaClave: String) {
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
                val mensajeError = when (e.errorSesion) {
                    ErrorSesion.CredencialesInvalidas -> "Usuario o contraseña incorrectos."
                    ErrorSesion.SesionExpirada -> "La sesión ha expirado. Inicie sesión nuevamente."
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
