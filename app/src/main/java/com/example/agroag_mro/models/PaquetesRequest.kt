package com.example.agroag_mro.models

import com.google.gson.annotations.SerializedName

data class PaquetesRequest(
    val login: Login,
    @SerializedName("Tipo") val tipo: String,
    @SerializedName("Paquete") val paq: String,
    @SerializedName("Orden") val ord: String


)