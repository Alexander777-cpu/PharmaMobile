package pe.edu.upeu.pharmamobile.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorIos

actual val platformModule: Module = module {
    // 1. Motor de Ktor para iOS (Darwin)
    single<HttpClientEngine> { Darwin.create() }

    // 2. URL local para simulador iOS (localhost con '/' al final)
    single(named("urlBase")) { "http://localhost:8080/api/v1/" }

    // 3. Capacidad nativa de compartir
    single<Compartidor> { CompartidorIos() }

    // 4. Capacidad nativa de almacenamiento seguro (Sesión 10)
    single<pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro> { 
        pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroIos() 
    }
}