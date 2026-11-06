package pe.edu.upeu.pharmamobile.platform

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pe.edu.upeu.pharmamobile.domain.platform.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.platform.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.platform.Permiso

class GestorPermisosAndroid(
    private val contexto: Context
) : GestorPermisos {

    private val prefs = contexto.getSharedPreferences("permisos_prefs", Context.MODE_PRIVATE)

    override fun estado(permiso: Permiso): EstadoPermiso {
        val permisoAndroid = mapPermiso(permiso)
        val concedido = ContextCompat.checkSelfPermission(contexto, permisoAndroid) == PackageManager.PERMISSION_GRANTED
        if (concedido) {
            return EstadoPermiso.Concedido
        }

        val yaSolicitado = prefs.getBoolean("solicitado_${permiso.name}", false)
        val actividad = PuentePermisos.obtenerActividad()

        return if (!yaSolicitado) {
            EstadoPermiso.Denegado
        } else if (actividad != null && !ActivityCompat.shouldShowRequestPermissionRationale(actividad, permisoAndroid)) {
            EstadoPermiso.DenegadoParaSiempre
        } else {
            EstadoPermiso.Denegado
        }
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        val estadoActual = estado(permiso)
        if (estadoActual == EstadoPermiso.Concedido) return EstadoPermiso.Concedido

        prefs.edit().putBoolean("solicitado_${permiso.name}", true).apply()
        val permisoAndroid = mapPermiso(permiso)
        val actividad = PuentePermisos.obtenerActividad()

        if (actividad != null) {
            ActivityCompat.requestPermissions(
                actividad,
                arrayOf(permisoAndroid),
                1001
            )
        }

        return estado(permiso)
    }

    override fun abrirAjustes() {
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", contexto.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            contexto.startActivity(intent)
        }
    }

    private fun mapPermiso(permiso: Permiso): String {
        return when (permiso) {
            Permiso.Camara -> Manifest.permission.CAMERA
        }
    }
}
