package pe.edu.upeu.pharmamobile.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroIos
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriverIos
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorIos

actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }

    single(named("urlBase")) { "http://localhost:8080/api/v1/" }

    single<Compartidor> { CompartidorIos() }

    single<AlmacenSeguro> { AlmacenSeguroIos() }

    single<CreadorDeDriver> { CreadorDeDriverIos() }
}
