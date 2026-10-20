package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.remote.AuthApi
import pe.edu.upeu.pharmamobile.data.remote.dto.ErrorResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobile.domain.error.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class AuthException(val errorSesion: ErrorSesion) : Exception()

class AuthRepositorioRest(
    private val api: AuthApi,
    private val almacen: AlmacenSeguro,
    private val cliente: HttpClient
) : AuthRepository {

    private val _mensajeExpiracion = MutableStateFlow<String?>(null)
    override val mensajeExpiracion: StateFlow<String?> = _mensajeExpiracion.asStateFlow()

    private var sesionActual: Sesion? = null

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        limpiarMensajeExpiracion()
        try {
            val dto = api.login(LoginRequestDto(credenciales.usuario, credenciales.clave))
            almacen.guardarToken(dto.token)
            
            cliente.authProvider<BearerAuthProvider>()?.clearToken()

            val sesion = dto.toDomain()
            sesionActual = sesion
            return sesion
        } catch (e: ClientRequestException) {
            val errorBody = runCatching { e.response.body<ErrorResponseDto>() }.getOrNull()
            val mensaje = errorBody?.message ?: errorBody?.error ?: e.message
            val errorSesion = mapearErrorSesion(e.response.status, mensaje)
            throw AuthException(errorSesion)
        } catch (e: Exception) {
            throw AuthException(ErrorSesion.SinConexion)
        }
    }

    override suspend fun sesionGuardada(): Sesion? {
        if (sesionActual != null) return sesionActual
        val token = almacen.leerToken()
        if (token != null) {
            sesionActual = Sesion(
                token = token,
                usuario = "usuario",
                nombreCompleto = "Usuario",
                permisos = emptySet()
            )
            return sesionActual
        }
        return null
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        runCatching<Unit> {
            cliente.authProvider<BearerAuthProvider>()?.clearToken()
        }
        sesionActual = null
    }

    override suspend fun expirarSesion(mensajeServidor: String?) {
        cerrarSesion()
        val mensajeProcesado = when {
            mensajeServidor?.contains("expirado", ignoreCase = true) == true ->
                "El token ha expirado. Inicie sesión nuevamente"
            mensajeServidor?.contains("deshabilitado", ignoreCase = true) == true ->
                "El usuario está deshabilitado"
            !mensajeServidor.isNullOrBlank() ->
                mensajeServidor
            else ->
                "El token ha expirado. Inicie sesión nuevamente"
        }
        _mensajeExpiracion.value = mensajeProcesado
    }

    override fun limpiarMensajeExpiracion() {
        _mensajeExpiracion.value = null
    }

    private fun mapearErrorSesion(estado: HttpStatusCode, mensaje: String?): ErrorSesion {
        return when (estado) {
            HttpStatusCode.Unauthorized -> {
                when {
                    mensaje?.contains("expirado", ignoreCase = true) == true -> ErrorSesion.SesionExpirada(mensaje)
                    mensaje?.contains("deshabilitado", ignoreCase = true) == true -> ErrorSesion.UsuarioDeshabilitado(mensaje)
                    !mensaje.isNullOrBlank() -> ErrorSesion.MensajeCustom(mensaje)
                    else -> ErrorSesion.CredencialesInvalidas
                }
            }
            HttpStatusCode.Forbidden -> ErrorSesion.SinPermiso
            else -> ErrorSesion.SinConexion
        }
    }
}
