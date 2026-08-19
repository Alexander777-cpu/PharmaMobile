package pe.edu.upeu.pharmamobile.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ClienteTest {
}

    @Test

    fun probarCliente(){
        val cliente= Cliente(
            id = 1L,
            nombre = "Farmacia Nueva Vida",
            correo = "ventas@central.pe",
            telefono = null
        )
        val resultado = cliente.ObtenerTelefono()

        assertEquals(
            expected = "923345600",
            actual = resultado
        )
}