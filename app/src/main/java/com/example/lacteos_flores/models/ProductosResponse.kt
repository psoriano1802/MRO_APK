package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ProductosResponse(
    val ResponseProductos: List<ItemProductos>? = null
)
data class ItemProductos(
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Nombre") val name: String? = null,
    @SerializedName("Unidad") val uni: String? = null,
    @SerializedName("Precio") val price: String? = null,
    @SerializedName("Naturaleza") val nat: String? = null,
    @SerializedName("TiempoEstTarea") val tst: String? = null,
    @SerializedName("CostoXHora") val cxh: String? = null,
    @SerializedName("CostoOperacion") val cxo: String? = null,
    @SerializedName("NumPersonaxOper") val npers: String? = null,
    @SerializedName("CostoUnitario") val costun: String? = null,
    @SerializedName("Err") val error: String? = null
)