package pe.edu.upeu.pharmamobile.platform

import android.app.Activity
import java.lang.ref.WeakReference

object PuentePermisos {
    private var actividadActual: WeakReference<Activity>? = null

    fun registrarActividad(activity: Activity) {
        actividadActual = WeakReference(activity)
    }

    fun obtenerActividad(): Activity? = actividadActual?.get()
}
