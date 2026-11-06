package pe.edu.upeu.pharmamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upeu.pharmamobile.platform.PuentePermisos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PuentePermisos.registrarActividad(this)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        PuentePermisos.registrarActividad(this)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
