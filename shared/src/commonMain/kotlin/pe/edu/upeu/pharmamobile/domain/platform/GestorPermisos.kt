package pe.edu.upeu.pharmamobile.domain.platform

enum class Permiso {
    Camara
}

sealed interface EstadoPermiso {
    data object Concedido : EstadoPermiso
    data object Denegado : EstadoPermiso
    data object DenegadoParaSiempre : EstadoPermiso
}

interface GestorPermisos {
    fun estado(permiso: Permiso): EstadoPermiso
    suspend fun solicitar(permiso: Permiso): EstadoPermiso
    fun abrirAjustes()
}
