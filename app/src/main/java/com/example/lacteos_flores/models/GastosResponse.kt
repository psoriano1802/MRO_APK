package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class GastosResponse (
    val Cat_gastosResponse: List<Trabajos>? = null
)

data class Trabajos (
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Descripcion")val descrp: String? = null
)