package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName


data class ProductosRequest(
    val login: Login,
    @SerializedName("Lista") val lista: String
)
