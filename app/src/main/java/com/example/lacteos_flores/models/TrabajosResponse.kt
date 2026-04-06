package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class TrabajosResponse (
    val ResponseTrabajos: List<Trabajos>? = null
)

data class Trabajos (
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Nombre")val name: String? = null,
    @SerializedName("Err")val err: String? = null
)
