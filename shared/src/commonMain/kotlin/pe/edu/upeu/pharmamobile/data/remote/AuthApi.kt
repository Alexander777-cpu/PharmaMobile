package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginResponseDto

class AuthApi(private val client: HttpClient) {
    suspend fun login(request: LoginRequestDto): LoginResponseDto =
        client.post("auth/login") {
            setBody(request)
        }.body()
}
