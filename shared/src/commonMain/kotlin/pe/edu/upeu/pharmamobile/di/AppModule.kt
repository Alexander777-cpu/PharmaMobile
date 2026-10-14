package pe.edu.upeu.pharmamobile.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel

private const val CATEGORIA_POR_DEFECTO = 1L

val dataModule = module {
    single { crearHttpClient(get(), get(named("urlBase"))) }
    single { ProductoApi(get()) }
    single { ProductoRepositorioEnMemoria() } // Se mantiene en Koin por si se necesita después
    single { ProductoRepositorioRest(get(), CATEGORIA_POR_DEFECTO) }

    // MODIFICACIÓN CLAVE: Enlazamos la interfaz directamente al backend REST.
    // Esto evita que la memoria temporal oculte los datos o errores reales de tu Base de Datos.
    single<ProductoRepository> { get<ProductoRepositorioRest>() }

    single<ClienteRepository> { ClienteRepositorioEnMemoria() }
}

val domainModule = module {
    factory { RegistrarProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
}

val presentationModule = module {
    viewModel {
        ProductoViewModel(
            registrarProducto = get(),
            actualizarProducto = get(),
            eliminarProducto = get(),
            listarProductos = get(),
            compartidor = getOrNull<Compartidor>() ?: object : Compartidor {
                override fun compartir(texto: String) {}
            }
        )
    }
    viewModel { ClienteViewModel(get(), get()) }
}

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        configuracionAdicional()
        modules(
            platformModule,
            dataModule,
            domainModule,
            presentationModule
        )
    }
}