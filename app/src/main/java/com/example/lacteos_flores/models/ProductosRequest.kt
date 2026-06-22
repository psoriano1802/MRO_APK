package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName


data class ProductosRequest(
    val login: Login,
    @SerializedName("Lista") val lista: String
)
data class existenciaReques(
    val login: Login,
    @SerializedName("Tipo") val tip: String
)