package pe.edu.upeu.pharmamobile.data.local

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class AlmacenSeguroIos : AlmacenSeguro {
    
    private val service = "pe.edu.upeu.pharmamobile"
    private val account = "jwt_token"
    private var ultimaSincronizacionTemp: Long? = null

    override fun guardarToken(token: String) {
        borrarToken() // Limpiamos si existía previamente
        
        @Suppress("CAST_NEVER_SUCCEEDS")
        val nsString = token as NSString
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding)
        
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrService] = service
        query[kSecAttrAccount] = account
        query[kSecValueData] = data

        @Suppress("UNCHECKED_CAST")
        SecItemAdd(query as CFDictionaryRef, null)
    }

    override fun leerToken(): String? {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrService] = service
        query[kSecAttrAccount] = account
        query[kSecReturnData] = true
        query[kSecMatchLimit] = kSecMatchLimitOne

        return memScoped {
            val result = alloc<CFTypeRefVar>()
            @Suppress("UNCHECKED_CAST")
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)
            
            if (status == errSecSuccess) {
                val data = CFBridgingRelease(result.value) as? NSData
                if (data != null) {
                    NSString.create(data, NSUTF8StringEncoding).toString()
                } else null
            } else null
        }
    }

    override fun borrarToken() {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrService] = service
        query[kSecAttrAccount] = account

        @Suppress("UNCHECKED_CAST")
        SecItemDelete(query as CFDictionaryRef)
    }

    override fun guardarUltimaSincronizacion(timestampMs: Long) {
        ultimaSincronizacionTemp = timestampMs
    }

    override fun leerUltimaSincronizacion(): Long? {
        return ultimaSincronizacionTemp
    }
}
