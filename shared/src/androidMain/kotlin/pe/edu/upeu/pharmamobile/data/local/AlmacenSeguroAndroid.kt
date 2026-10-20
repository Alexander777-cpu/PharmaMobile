package pe.edu.upeu.pharmamobile.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {

    private val masterKey = MasterKey.Builder(contexto)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        contexto,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun guardarToken(token: String) {
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }

    override fun leerToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    override fun borrarToken() {
        sharedPreferences.edit().remove("jwt_token").apply()
    }
}
