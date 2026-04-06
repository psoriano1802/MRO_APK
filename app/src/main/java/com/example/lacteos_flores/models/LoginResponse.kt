package com.example.lacteos_flores.models

data class LoginResponse(
    val LoginResponse: List<LoginResult>
)
    data class LoginResult(
        val ok: String?  = null,
        val User: String? = null,
        val Sucursal: String? = null,
        val NoSucursal: String? = null,
        val NoAlmacen: String? = null,
        val Almacen: String? = null,
        val Msn: String? = null
   )

data class pantallas(
    val pantalla: String? = null
)