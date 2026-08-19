package pe.edu.upeu.pharmamobile.domain.model

data class Cliente(
    val id: Long,
    val nombre: String,
    val correo: String,
    val telefono: String?
){
    fun ObtenerTelefono(): String{
        return telefono?: "No registrado"
    }
}
