package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.plugins.logging.Logger

actual val ktorLogger: Logger = object : Logger {
    override fun log(message: String) {
        println("[KTOR_HTTP] $message")
    }
}
