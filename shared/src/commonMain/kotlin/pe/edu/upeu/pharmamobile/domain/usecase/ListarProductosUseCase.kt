package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ListarProductosUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(): Result<List<Producto>> = resultadoDe {
        productoRepository.listar()
    }
}