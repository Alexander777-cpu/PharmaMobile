package pe.edu.upeu.pharmamobile.platform

import platform.UIKit.UIDevice

actual class InfoDispositivo actual constructor() {
    actual val sistema: String = UIDevice.currentDevice.systemName
    actual val version: String = UIDevice.currentDevice.systemVersion
    actual val modelo: String = UIDevice.currentDevice.model

    actual fun obtenerTexto(): String {
        return "$sistema $version ($modelo)"
    }
}