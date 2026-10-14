package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class EliminarProductoUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = resultadoDe {
        productoRepository.eliminar(id)
    }
}