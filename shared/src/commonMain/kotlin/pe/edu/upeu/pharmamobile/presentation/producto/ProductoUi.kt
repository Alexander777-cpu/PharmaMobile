package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.platform.formatearSoles

data class ProductoUi(
    val id: String,
    val nombre: String,
    val precio: String,
    val stock: String,
    val requiereReposicion: Boolean
)

fun Producto.toUi(): ProductoUi {
    return ProductoUi(
        id = this.id.toString(),
        nombre = this.nombre,
        precio = formatearSoles(this.precio),
        stock = "${this.stock} uds.",
        requiereReposicion = this.stock < 10
    )
}