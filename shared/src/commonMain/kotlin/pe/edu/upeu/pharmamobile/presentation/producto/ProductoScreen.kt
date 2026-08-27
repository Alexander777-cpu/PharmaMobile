package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Producto

@Composable
fun ProductoScreen() {
    // Declaración de estados de los campos de texto
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    // Estados para retroalimentación y control de errores
    var mensaje by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }
    var intentoRegistrar by remember { mutableStateOf(false) }

    // Banderas para activar el borde rojo (isError) en los campos
    val nombreEsInvalido = intentoRegistrar && nombre.isBlank()
    val precioEsInvalido = intentoRegistrar && (precio.toDoubleOrNull() == null || (precio.toDoubleOrNull() ?: 0.0) <= 0.0)
    val stockEsInvalido = intentoRegistrar && (stock.toIntOrNull() == null || (stock.toIntOrNull() ?: 0) < 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PharmaMobil",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Registro de Producto",
            style = MaterialTheme.typography.titleMedium
        )

        // Campo: Nombre del Producto
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del producto") },
            isError = nombreEsInvalido,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo: Precio
        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio") },
            isError = precioEsInvalido,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo: Stock
        OutlinedTextField(
            value = stock,
            onValueChange = { stock = it },
            label = { Text("Stock") },
            isError = stockEsInvalido,
            modifier = Modifier.fillMaxWidth()
        )

        // Botón Registrar con las 5 reglas funcionales de validación
        Button(
            onClick = {
                intentoRegistrar = true
                val precioDouble = precio.toDoubleOrNull()
                val stockInt = stock.toIntOrNull()

                when {
                    // Regla 1: Nombre no vacío ni solo espacios
                    nombre.isBlank() -> {
                        mensaje = "El nombre es obligatorio."
                        esError = true
                    }
                    // Regla 2: Conversión numérica de precio
                    precioDouble == null -> {
                        mensaje = "Ingrese un precio numérico."
                        esError = true
                    }
                    // Regla 3: Precio mayor a 0 (> 0.0)
                    precioDouble <= 0.0 -> {
                        mensaje = "El precio debe ser mayor que cero."
                        esError = true
                    }
                    // Regla 4: Conversión de stock a entero
                    stockInt == null -> {
                        mensaje = "Ingrese un stock entero."
                        esError = true
                    }
                    // Regla 5: Stock mayor o igual a 0 (>= 0)
                    stockInt < 0 -> {
                        mensaje = "El stock no puede ser negativo."
                        esError = true
                    }
                    // Caso exitoso
                    else -> {
                        val nuevoProducto = Producto(
                            id = 1L,
                            nombre = nombre.trim(),
                            precio = precioDouble,
                            stock = stockInt
                        )

                        mensaje = "Producto '${nuevoProducto.nombre}' registrado con éxito."
                        esError = false

                        // Limpieza automática del formulario
                        nombre = ""
                        precio = ""
                        stock = ""
                        intentoRegistrar = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTRAR")
        }

        // Tarjeta de mensaje del sistema
        mensaje?.let { textoMensaje ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (esError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = textoMensaje,
                    modifier = Modifier.padding(12.dp),
                    color = if (esError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}