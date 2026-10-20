package pe.edu.upeu.pharmamobile.domain.model


import kotlin.test.Test
import kotlin.test.assertEquals

class ClienteTest {

    // Prueba de Null-Safety (Sesión 2 - Dominio)
    @Test
    fun probarCliente() {
        val cliente = Cliente(
            id = 1L,
            nombre = "Farmacia Nueva Vida",
            correo = "ventas@central.pe",
            telefono = null
        )
        val resultado = cliente.telefono ?: "No registrado"

        assertEquals(
            expected = "No registrado",
            actual = resultado
        )
    }

// Se ha comentado esta prueba porque el servicio 'ProductoService' original
    // ya no existe en la arquitectura actual, la cual usa UseCases y Repositories (Sesiones 7, 8, 9).
    /*
    @Test
    fun probarCorrutinasYFlow() = runBlocking {
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
    */
}