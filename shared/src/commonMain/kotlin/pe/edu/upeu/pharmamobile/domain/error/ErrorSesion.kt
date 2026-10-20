package pe.edu.upeu.pharmamobile.domain.error

sealed interface ErrorSesion {
    data object CredencialesInvalidas : ErrorSesion
    data class SesionExpirada(val mensaje: String? = null) : ErrorSesion
    data class UsuarioDeshabilitado(val mensaje: String? = null) : ErrorSesion
    data class MensajeCustom(val mensaje: String) : ErrorSesion
    data object SinPermiso : ErrorSesion
    data object SinConexion : ErrorSesion
}
