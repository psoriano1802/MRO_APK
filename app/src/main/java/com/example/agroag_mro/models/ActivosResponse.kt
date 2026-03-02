package com.example.agroag_mro.models

import com.google.gson.annotations.SerializedName

data class ActivosResponse (
    val ResponseActivos: List<Activos>? = null
)
data class Activos (
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Nombre") val name: String? = null,
    @SerializedName("Status") val status: String? = null,
    @SerializedName("TipoActivo") val tipo: String? = null,
    @SerializedName("AnioUni_ModActivos") val auma: String? = null,
    @SerializedName("ModeloUni") val modelo: String? = null,
    @SerializedName("Serie") val serie: String? = null,
    @SerializedName("CCosto") val costo: String? = null,
    @SerializedName("Fabricante") val fabricante: String? = null,
    @SerializedName("GarantiaVence") val garantia: String? = null,
    @SerializedName("Color") val color: String? = null,
    @SerializedName("Placas") val placa: String? = null,
    @SerializedName("Kilometraje") val kilometraje: String? = null,
    @SerializedName("Direccion1") val dir1: String? = null,
    @SerializedName("Direccion2") val dir2: String? = null,
    @SerializedName("Colonia") val colonia: String? = null,
    @SerializedName("Poblacion") val poblacion: String? = null,
    @SerializedName("Ubicacion") val ubicacion: String? = null,
    @SerializedName("Err") val err: String? = null
)