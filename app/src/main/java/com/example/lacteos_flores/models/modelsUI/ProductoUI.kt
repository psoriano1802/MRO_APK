package com.example.lacteos_flores.models.modelsUI

data class LoteDetalle(
    var lote: String,
    var cantidad: Double,
    var talla: String = "-",
    var modelo: String = "-",
    var color: String = "-"
)

data class ProductoUI(
    val cve: String? = null,
    var cant: Double? = null,
    val uni: String? = null,
    var costuni: Double? = null,
    var costbase: Double? = null,
    var importe: Double? = null,
    val descripcion: String? = null,
    val minutos: Double? = null,
    val horas: Double? = null,
    var lote: String? = null,
    var talla: String = "-",
    var modelo: String = "-",
    var color: String = "-",
    var tmc: String? = "0",
    var desgloseLotes: MutableList<LoteDetalle> = mutableListOf()
)
