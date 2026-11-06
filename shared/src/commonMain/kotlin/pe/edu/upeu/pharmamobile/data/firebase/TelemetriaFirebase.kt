package pe.edu.upeu.pharmamobile.data.firebase

import pe.edu.upeu.pharmamobile.domain.port.Telemetria
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

class TelemetriaFirebase : Telemetria {
    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        try {
            contexto.forEach { (clave, valor) ->
                Firebase.crashlytics.setCustomKey(clave, valor)
            }
            Firebase.crashlytics.recordException(e)
        } catch (t: Throwable) {
            println("[Telemetria Error]: ${e.message} | Contexto: $contexto (Fallo Firebase:${t.message})")
        }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        try {
            val detalle = datos.entries.joinToString(separator = ", ") { "${it.key}=${it.value}" }
            Firebase.crashlytics.log("EVENTO: $nombre | $detalle")
        } catch (t: Throwable) {
            println("[Telemetria Evento]: $nombre | $datos (Fallo Firebase:${t.message})")
        }
    }
}
