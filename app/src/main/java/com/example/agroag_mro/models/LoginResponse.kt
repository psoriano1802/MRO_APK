package com.example.agroag_mro.models

data class LoginResponse(
    val LoginResponse: List<LoginResult>
)
    data class LoginResult(
        val ok: String?  = null,
        val usuario: String? = null,
        val nombre: String? = null,
        val estatus: String? = null,
        val estatus_mro: String? = null,
        val perfil_mro: String? = null,
        val sucursal: String? = null,
        val accesos: List<pantallas>? = null,
        val Err: String? = null
   )

data class pantallas(
    val pantalla: String? = null
)