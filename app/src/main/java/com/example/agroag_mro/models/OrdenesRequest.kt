package com.example.agroag_mro.models

import com.google.gson.annotations.SerializedName

data class OrdenesRequest (
    val login: Login,
    @SerializedName("Fecha_Inicio") val fi: String,
    @SerializedName("Fecha_Fin") val ff: String,
    @SerializedName("Sucursal") val suc: String,
    @SerializedName("atrasadas") val atras: String
)