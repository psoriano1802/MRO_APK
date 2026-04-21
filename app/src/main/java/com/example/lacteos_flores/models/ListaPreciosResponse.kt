package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ListaPreciosResponse (
    val ListaPreciosResponse: List<listapr>? = null

)
data class listapr (
    val ok: String? = null,
    @SerializedName("Lista") val cvelist: String? = null,
    val Productos: List<listprod>?= null
)

data class listprod(
    @SerializedName("clave") val cve: String? = null,
    @SerializedName("descricion") val descrp: String? = null,
    @SerializedName("precio") val precio: String? = null,
    @SerializedName("unidad") val uni: String? = null,
    @SerializedName("comentarios") val come: String? = null,
    @SerializedName("zona") val zona: String? = null
)