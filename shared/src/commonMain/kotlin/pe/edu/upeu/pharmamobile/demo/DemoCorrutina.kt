package pe.edu.upeu.pharmamobile.demo

import kotlinx.coroutines.runBlocking
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos
import pe.edu.upeu.pharmamobile.domain.service.ProductoService

fun main() = runBlocking {
    val service = ProductoService()

    println("=== 1. Prueba de Función Suspend (Paso 4 & 5) ===")
    val productos = service.obtenerProductos()
    println("Total productos obtenidos: ${productos.size}")

    println("\n=== 2. Prueba de Flow con Sealed Classes (Pasos 15 a 17) ===")
    service.cargarProductos().collect { estado ->
        when (estado) {
            is ResultadoProductos.Cargando -> println("Estado: [CARGANDO DATOS...]")
            is ResultadoProductos.Exito -> {
                println("Estado: [ÉXITO] - ${estado.list.size} productos cargados:")
                estado.list.forEach {
                    println("  * ${it.nombre} - S/. ${it.precio} | Stock: ${it.stock}")
                }
            }
            is ResultadoProductos.Error -> println("Estado: [ERROR] - ${estado.msg}")
        }
    }
}