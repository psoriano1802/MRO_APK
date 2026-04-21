package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ProductosResponse(
    val ResponseProductos: List<ItemProductos>? = null
)
data class ItemProductos(
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Descripcion") val name: String? = null,
    @SerializedName("CodigoBarras") val cb: String? = null,
    @SerializedName("Unidad") val uni: String? = null,
    @SerializedName("PrimerPrecio") val precio: String? = null,
    @SerializedName("IVA") val iva: String? = null,
    @SerializedName("UbicacionAlmacen") val ubicalm: String? = null,
    @SerializedName("BanderaSustituto") val bsust: String? = null,
    @SerializedName("Serie") val serie: String? = null,
    @SerializedName("LoteSF") val lote: String? = null,
    @SerializedName("TMC") val tmc: String? = null,
    @SerializedName("Ubicacion") val ubicacion: String? = null,
    @SerializedName("Pedimento") val pedimento: String? = null,
    @SerializedName("Err") val error: String? = null
)