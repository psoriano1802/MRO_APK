package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class GastosResponse (
    val Cat_gastosResponse: List<gastos>? = null
)

data class gastos (
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Descripcion")val descrp: String? = null,
    @SerializedName("err") val err: String? = null
)