package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.platform.formatearSoles
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi

fun Producto.comoTextoParaCompartir(): String =
    "$nombre ${formatearSoles(precio)} | Stock: $stock"

fun ProductoUi.comoTextoParaCompartir(): String =
    "$nombre $precio | Stock: $stock"