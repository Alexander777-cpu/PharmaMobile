package pe.edu.upeu.pharmamobile.domain.error

sealed interface ErrorApi {
    data class Validacion(val porCampo: Map<String, String>) : ErrorApi
    data object NoEncontrado : ErrorApi
    data class Conflicto(val mensaje: String) : ErrorApi
    data class SesionExpirada(val mensaje: String? = null) : ErrorApi // HTTP 401 Unauthorized con mensaje del servidor
    data object SinPermiso : ErrorApi     // HTTP 403 Forbidden
    data object NoAutorizado : ErrorApi   // Fallback general
    data object Servidor : ErrorApi
    data object SinConexion : ErrorApi
    data object TiempoAgotado : ErrorApi
}

class ErrorApiException(val error: ErrorApi) : Exception()
