package pe.edu.upeu.pharmamobile.data.mapper

import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobile.domain.model.Producto

fun ProductoResponseDto.toDomain(): Producto = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock
)

fun Producto.toRequest(categoriaId: Long = 1L): ProductoRequestDto = ProductoRequestDto(
    nombre = nombre,
    precio = precio,
    stock = stock,
    estado = true,
    categoriaId = categoriaId
)
