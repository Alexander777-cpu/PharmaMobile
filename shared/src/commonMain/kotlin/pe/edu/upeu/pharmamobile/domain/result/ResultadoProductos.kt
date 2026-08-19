package pe.edu.upeu.pharmamobile.domain.result

import pe.edu.upeu.pharmamobile.domain.model.Producto

// Pasos 6 a 8: Manejo de Resultados con Sealed Class
sealed class ResultadoProductos {
    data object Cargando : ResultadoProductos()
    data class Exito(val list: List<Producto>) : ResultadoProductos()
    data class Error(val msg: String) : ResultadoProductos()
}