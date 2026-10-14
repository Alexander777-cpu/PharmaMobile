package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Cliente
import pe.edu.upeu.pharmamobile.domain.repository.ClienteRepository

class ListarClientesUseCase(
    private val clienteRepository: ClienteRepository
) {
    suspend operator fun invoke(): Result<List<Cliente>> = resultadoDe {
        clienteRepository.listar()
    }
}