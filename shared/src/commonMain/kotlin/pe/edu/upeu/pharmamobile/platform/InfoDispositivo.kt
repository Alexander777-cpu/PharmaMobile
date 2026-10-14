package pe.edu.upeu.pharmamobile.platform

expect class InfoDispositivo() {
    val sistema: String
    val version: String
    val modelo: String
    fun obtenerTexto(): String
}