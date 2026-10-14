## Código específico de plataforma

En cumplimiento con la arquitectura Kotlin Multiplatform, el proyecto aísla las llamadas a las APIs nativas mediante el patrón `expect` / `actual`:

| Capacidad | Firma expect | Implementación Android | Implementación iOS |
| :--- | :--- | :--- | :--- |
| **Formato de moneda** | `fun formatearSoles(valor: Double): String` | `NumberFormat` con `Locale("es", "PE")` | `NSNumberFormatter` con `NSLocale("es_PE")` |
| **Compartir producto** | `interface Compartidor` | `Intent.ACTION_SEND` (`CompartidorAndroid.kt`) | `UIActivityViewController` (`CompartidorIos.kt`) |
| **Inyección nativa** | `val platformModule: Module` | Módulo Koin con contexto Android (`PlatformModule.android.kt`) | Módulo Koin sin contexto (`PlatformModule.ios.kt`) |
| **Información del dispositivo** | `expect class InfoDispositivo` | `android.os.Build` (`InfoDispositivo.android.kt`) | `UIDevice.currentDevice` (`InfoDispositivo.ios.kt`) |