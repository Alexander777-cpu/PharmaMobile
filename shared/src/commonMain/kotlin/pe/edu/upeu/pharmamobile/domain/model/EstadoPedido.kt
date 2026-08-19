package pe.edu.upeu.pharmamobile.domain.model

sealed class EstadoPedido{
    data object Pendiente : EstadoPedido()
    data object Procesando : EstadoPedido()
    data object Entregando : EstadoPedido()
    data class Rechazado(
        val motivo: String
    ): EstadoPedido()
}
