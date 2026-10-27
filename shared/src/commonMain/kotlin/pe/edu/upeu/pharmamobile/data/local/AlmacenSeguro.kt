package pe.edu.upeu.pharmamobile.data.local

interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()

    fun guardarUltimaSincronizacion(timestampMs: Long)
    fun leerUltimaSincronizacion(): Long?
}
