package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ubicacionRequest (
    val login: Login,
    @SerializedName("RFC") val rfc: String? = null,
    @SerializedName("latiInicio") val li: String? = null,
    @SerializedName("lonInicio") val lo: String? = null,
    @SerializedName("direInicio") val dire: String? = null,
    @SerializedName("nomUser") val nom: String? = null,
    @SerializedName("latiFin") val lf: String? = null,
    @SerializedName("lonFin") val lof: String? = null,
    @SerializedName("direFin") val diref: String? = null
)

