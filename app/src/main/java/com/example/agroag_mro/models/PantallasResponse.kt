package com.example.agroag_mro.models

import com.google.gson.annotations.SerializedName

data class PantallasResponse (
    val ResponsePantallas: List<itemScreen>
    )

data class itemScreen(
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Nombre") val name: String? = null,
    @SerializedName("Err") val err: String? = null


)