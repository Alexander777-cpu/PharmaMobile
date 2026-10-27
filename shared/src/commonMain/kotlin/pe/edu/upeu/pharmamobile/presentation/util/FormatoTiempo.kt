package pe.edu.upeu.pharmamobile.presentation.util

fun formatearAntiguedadSincronizacion(
    timestampMs: Long?,
    actualMs: Long = obtenerTiempoActualMs()
): String {
    if (timestampMs == null || timestampMs <= 0L) {
        return "Nunca sincronizado"
    }

    val diferenciaMs = actualMs - timestampMs
    if (diferenciaMs < 0) return "Actualizado hace unos instantes"

    val segundos = diferenciaMs / 1000
    val minutos = segundos / 60
    val horas = minutos / 60
    val dias = horas / 24

    return when {
        minutos < 1 -> "Actualizado hace unos instantes"
        minutos < 60 -> if (minutos == 1L) "Actualizado hace 1 minuto" else "Actualizado hace $minutos minutos"
        horas < 24 -> if (horas == 1L) "Actualizado hace 1 hora" else "Actualizado hace $horas horas"
        else -> if (dias == 1L) "Actualizado hace 1 día" else "Actualizado hace $dias días"
    }
}
