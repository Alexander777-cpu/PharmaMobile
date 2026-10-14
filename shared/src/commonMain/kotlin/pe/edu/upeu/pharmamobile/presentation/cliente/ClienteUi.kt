package pe.edu.upeu.pharmamobile.presentation.cliente

import pe.edu.upeu.pharmamobile.domain.model.Cliente

data class ClienteUi(
    val id: Long,
    val nombre: String,
    val correo: String,
    val telefono: String
)

fun Cliente.aUi(): ClienteUi = ClienteUi(
    id = id,
    nombre = nombre,
    correo = correo,
    telefono = telefono ?: TELEFONO_AUSENTE
)

const val TELEFONO_AUSENTE = "No registrado"
