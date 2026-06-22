package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ResponseExistencia(
    val ResponseExistencia: List<existencia>
)
data class existencia(
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Auxiliar") val aux: String? = null,
    @SerializedName("Existencia") val exist: String? = null,
    @SerializedName("Fecha") val fec: String? = null,
    @SerializedName("Talla") val talla: String? = null,
    @SerializedName("Modelo") val modelo: String? = null,
    @SerializedName("Color") val color: String? = null,
    @SerializedName("Err") val err: String? = null
)