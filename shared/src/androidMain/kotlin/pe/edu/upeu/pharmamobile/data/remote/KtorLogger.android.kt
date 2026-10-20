package pe.edu.upeu.pharmamobile.data.remote

import android.util.Log
import io.ktor.client.plugins.logging.Logger

actual val ktorLogger: Logger = object : Logger {
    override fun log(message: String) {
        Log.d("KTOR_HTTP", message)
    }
}
