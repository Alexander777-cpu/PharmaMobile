package pe.edu.upeu.pharmamobile

import android.app.Application
import pe.edu.upeu.pharmamobile.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}