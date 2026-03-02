package com.example.agroag_mro.models

import com.google.gson.annotations.SerializedName


data class ProductosRequest(
    val login: Login,
    @SerializedName("Tipo") val tipo: String,
    @SerializedName("Browser") val busca: String
)
