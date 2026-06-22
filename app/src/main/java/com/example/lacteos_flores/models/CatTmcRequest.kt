package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class CatTmcRequest(
    val login: Login,
    @SerializedName("Tabla") val tabla: String
)
