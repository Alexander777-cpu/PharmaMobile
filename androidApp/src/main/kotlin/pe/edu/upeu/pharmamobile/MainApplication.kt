package pe.edu.upeu.pharmamobile

import android.app.Application
import com.google.firebase.FirebaseApp
import pe.edu.upeu.pharmamobile.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        initKoin()
    }
}
