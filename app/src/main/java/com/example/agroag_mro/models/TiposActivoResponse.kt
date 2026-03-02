package com.example.agroag_mro.models

import com.google.gson.annotations.SerializedName

data class TiposActivoResponse (
    val ResponseTiposActivos: List<TiposActivo>? = null

)
data class TiposActivo (
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Nombre") val name: String? = null,
    @SerializedName("Err") val err: String? = null
)