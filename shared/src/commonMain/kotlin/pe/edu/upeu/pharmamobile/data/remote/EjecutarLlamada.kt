package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException // Corrección del paquete deprecado
import pe.edu.upeu.pharmamobile.data.remote.dto.ErrorResponseDto
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException

suspend fun <T> ejecutarLlamada(bloque: suspend () -> T): T {
    try {
        return bloque()
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (e: ClientRequestException) {
        throw ErrorApiException(traducirCliente(e))
    } catch (e: ServerResponseException) {
        throw ErrorApiException(ErrorApi.Servidor)
    } catch (e: HttpRequestTimeoutException) {
        throw ErrorApiException(ErrorApi.TiempoAgotado)
    } catch (e: IOException) {
        throw ErrorApiException(ErrorApi.SinConexion)
    }
}

private suspend fun traducirCliente(e: ClientRequestException): ErrorApi {
    val cuerpo = runCatching { e.response.body<ErrorResponseDto>() }.getOrNull()
    return when (e.response.status.value) {
        400 -> ErrorApi.Validacion(cuerpo?.validationErrors.orEmpty())
        404 -> ErrorApi.NoEncontrado
        409 -> ErrorApi.Conflicto(cuerpo?.message ?: "Operación no permitida")
        401, 403 -> ErrorApi.NoAutorizado
        else -> ErrorApi.Servidor
    }
}