package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ControlAuxResponse(
    val ControlAuxResponse: List<ctlauxiliar>
)
data class ctlauxiliar(
    val ok: String?= null,
    @SerializedName("Clave") val cve: String?= null,
    @SerializedName("Cantidad") val cant: String?= null,
    @SerializedName("Lote_Serie") val lt: String?= null,
    @SerializedName("Pedimento") val pedi: String?= null,
    @SerializedName("Ubicacion") val ubica: String?= null,
    @SerializedName("Talla") val talla: String?= null,
    @SerializedName("Modelo") val modelo: String?= null,
    @SerializedName("Color") val color: String?= null,
    @SerializedName("Caducidad") val caduca: String?= null,
    @SerializedName("Err") val err: String?= null
)