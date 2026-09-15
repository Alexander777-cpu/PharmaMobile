package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Cliente

interface ClienteRepository {
    suspend fun registrar(cliente: Cliente): Cliente
    suspend fun listar(): List<Cliente>
}