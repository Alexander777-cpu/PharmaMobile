package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto

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
        precio = "S/ ${this.precio}",
        stock = "${this.stock} uds.",
        requiereReposicion = this.stock < 10
    )
}