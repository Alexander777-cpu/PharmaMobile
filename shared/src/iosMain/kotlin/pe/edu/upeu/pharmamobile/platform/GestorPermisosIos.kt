package pe.edu.upeu.pharmamobile.platform

import pe.edu.upeu.pharmamobile.domain.platform.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.platform.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.platform.Permiso

class GestorPermisosIos : GestorPermisos {
    override fun estado(permiso: Permiso): EstadoPermiso = EstadoPermiso.Concedido
    override suspend fun solicitar(permiso: Permiso): EstadoPermiso = EstadoPermiso.Concedido
    override fun abrirAjustes() {}
}
