package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.Sesion

interface AuthRepository {
    val mensajeExpiracion: StateFlow<String?>
    suspend fun iniciarSesion(credenciales: Credenciales): Sesion
    suspend fun sesionGuardada(): Sesion?
    suspend fun cerrarSesion()
    suspend fun expirarSesion(mensajeServidor: String? = null)
    fun limpiarMensajeExpiracion()
}
