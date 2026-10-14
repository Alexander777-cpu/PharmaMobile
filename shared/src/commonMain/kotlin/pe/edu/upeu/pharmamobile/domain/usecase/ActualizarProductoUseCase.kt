package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ActualizarProductoUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(
        id: Long,
        nombre: String,
        precio: String,
        stock: String
    ): Result<Producto> {
        val errores = ErroresDeProducto(
            nombre = validarNombre(nombre),
            precio = validarPrecio(precio),
            stock = validarStock(stock)
        )

        if (errores.hayErrores) {
            return Result.failure(ProductoInvalidoException(errores))
        }

        return resultadoDe {
            productoRepository.actualizar(
                Producto(
                    id = id,
                    nombre = nombre.trim(),
                    precio = precio.toDouble(),
                    stock = stock.toInt()
                )
            )
        }
    }

    private fun validarNombre(nombre: String): String? =
        if (nombre.isBlank()) "El nombre es obligatorio" else null

    private fun validarPrecio(precio: String): String? {
        val valor = precio.toDoubleOrNull()
        return when {
            precio.isBlank() -> "El precio es obligatorio"
            valor == null || !valor.isFinite() -> "El precio debe ser un número válido"
            valor <= 0 -> "El precio debe ser mayor a 0"
            else -> null
        }
    }

    private fun validarStock(stock: String): String? {
        val valor = stock.toIntOrNull()
        return when {
            stock.isBlank() -> "El stock es obligatorio"
            valor == null -> "El stock debe ser un número entero"
            valor < 0 -> "El stock no puede ser negativo"
            else -> null
        }
    }
}