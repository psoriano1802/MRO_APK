package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class GetClientesResponse(
    val GetClientesResponse: List<cliente>
)
data class cliente(
    val ok: String?=null,
    @SerializedName("Clave") val cve: String?= null,
    @SerializedName("Nombre") val name: String?= null,
    @SerializedName("RFC") val rfc: String?= null,
    @SerializedName("LimiteCredito") val limcre: String?= null,
    @SerializedName("Plazo") val plazo: String?= null,
    @SerializedName("Calle") val calle: String?= null,
    @SerializedName("Colonia") val colo: String?= null,
    @SerializedName("Poblacion") val pobl: String?= null,
    @SerializedName("Telefono") val tel: String?= null,
    @SerializedName("CP") val cp: String?= null,
    @SerializedName("Agente") val agent: String?= null,
    @SerializedName("Latitud") val latitud: String?= null,
    @SerializedName("Longitud") val longitud: String?= null,
    @SerializedName("FLunes") val flunes: String?= null,
    @SerializedName("FMartes") val fmartes: String?= null,
    @SerializedName("FMiercoles") val fmiercoles: String?= null,
    @SerializedName("FJueves") val fjueves: String?= null,
    @SerializedName("FViernes") val fviernes: String?=null,
    @SerializedName("FSabado") val fsabado: String?= null,
    @SerializedName("FDomingo") val fdomingo: String?= null,
    @SerializedName("Lunes") val lunes: String?= null,
    @SerializedName("Martes") val martes: String?= null,
    @SerializedName("Miercoles") val miercoles: String?= null,
    @SerializedName("Jueves") val jueves: String?= null,
    @SerializedName("Viernes") val viernes: String?= null,
    @SerializedName("Sabado") val sabado: String?= null,
    @SerializedName("Domingo") val domingo: String?= null,
    @SerializedName("Descuento_Esp") val descuentop: String?= null,
    @SerializedName("Comentarios") val come: String?=null,
    @SerializedName("ListaPrecio") val lprecio: String?=null
)