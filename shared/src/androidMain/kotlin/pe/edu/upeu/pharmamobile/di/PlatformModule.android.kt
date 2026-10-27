package pe.edu.upeu.pharmamobile.di

import android.annotation.SuppressLint
import android.content.Context
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriverAndroid
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorAndroid

@SuppressLint("PrivateApi")
private fun obtenerContextoSeguro(): Context {
    return Class.forName("android.app.ActivityThread")
        .getMethod("currentApplication")
        .invoke(null) as Context
}

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }

    single(named("urlBase")) { "http://10.0.2.2:8080/api/v1/" }

    single<Compartidor> {
        val contexto = runCatching { androidContext() }.getOrElse { obtenerContextoSeguro() }
        CompartidorAndroid(contexto)
    }

    single<AlmacenSeguro> {
        val contexto = runCatching { androidContext() }.getOrElse { obtenerContextoSeguro() }
        AlmacenSeguroAndroid(contexto)
    }

    single<CreadorDeDriver> {
        val contexto = runCatching { androidContext() }.getOrElse { obtenerContextoSeguro() }
        CreadorDeDriverAndroid(contexto)
    }
}
