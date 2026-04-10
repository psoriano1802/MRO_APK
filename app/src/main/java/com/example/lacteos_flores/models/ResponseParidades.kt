package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ResponseParidades(
    val ResponseParidades: List<paridades>? = null
)
data class paridades(
    val ok: String?=null,
    @SerializedName("Moneda") val mon: String?=null,
    @SerializedName("Fecha") val fecha: String?=null,
    @SerializedName("Hora") val hora: String?=null,
    @SerializedName("Paridad") val paridad: String?=null
)
