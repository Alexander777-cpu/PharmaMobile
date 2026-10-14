package pe.edu.upeu.pharmamobile.di

import android.annotation.SuppressLint
import android.content.Context
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorAndroid

@SuppressLint("PrivateApi")
private fun obtenerContextoSeguro(): Context {
    return Class.forName("android.app.ActivityThread")
        .getMethod("currentApplication")
        .invoke(null) as Context
}

actual val platformModule: Module = module {
    // 1. Motor de Ktor para Android
    single<HttpClientEngine> { OkHttp.create() }

    // 2. URL base con barra '/' al final para que Ktor no borre 'v1'
    single(named("urlBase")) { "http://10.0.2.2:8080/api/v1/" }

    // 3. Capacidad nativa de compartir (Sesión 9)
    single<Compartidor> {
        val contexto = runCatching { androidContext() }.getOrElse { obtenerContextoSeguro() }
        CompartidorAndroid(contexto)
    }
}