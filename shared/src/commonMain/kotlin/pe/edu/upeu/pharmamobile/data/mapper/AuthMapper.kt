package pe.edu.upeu.pharmamobile.data.mapper

import pe.edu.upeu.pharmamobile.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobile.domain.model.Sesion

fun LoginResponseDto.toDomain(): Sesion {
    return Sesion(
        token = token,
        usuario = username,
        nombreCompleto = nombreCompleto,
        permisos = permisos.toSet()
    )
}
