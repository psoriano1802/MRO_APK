package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class BancosResponse(
    val BancosResponse: List<bancos>

)
data class bancos(
    val ok: String?,
    @SerializedName("Clave")val cve: String?,
    @SerializedName("Banco")val ban: String?
)
