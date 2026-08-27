package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Muestra la pantalla de Producto
            ProductoScreen()
        }
    }
}