package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class AltaDoctosRequest (
    val login: Login,
    @SerializedName("RFC") val rfcEmpresa: String? = null,
    @SerializedName("k_sucursal") val suc: String,
    @SerializedName("k_almacen") val alm: String,
    @SerializedName("k_genero") val gen: String,
    @SerializedName("k_naturaleza") val nat: String,
    @SerializedName("k_grupo") val grp: String,
    @SerializedName("k_tipo") val tipo: String,
    @SerializedName("k_fecha") val fecha: String,
    @SerializedName("k_clave") val claveCliente: String? = null,
    @SerializedName("k_moneda") val moneda: String,
    @SerializedName("k_paridad") val paridad: String,
    @SerializedName("k_rfc") val rfcCliente: String? = null,
    @SerializedName("k_nombre") val nombreCliente: String? = null,
    @SerializedName("k_ieps") val ieps: String? = null,
    @SerializedName("k_iva") val iva: String? = null,
    @SerializedName("k_refer") val refer: String? = null,
    @SerializedName("k_coment") val comenta: String,
    @SerializedName("k_monto") val monto: String,
    @SerializedName("k_plazo") val plazo: String? = null,
    @SerializedName("k_vence") val vence: String? = null,
    @SerializedName("k_cond") val cond: String? = null,
    @SerializedName("k_agente") val agente: String? = null,
    @SerializedName("k_84") val lati: String? = null,
    @SerializedName("k_85") val longi: String? = null,
    @SerializedName("k_proyecto") val proyecto: String? = null,
    @SerializedName("k_solicita") val solicita: String? = null,
    @SerializedName("k_saldo") val saldo: String? = null,
    @SerializedName("k_depto") val depto: String? = null,
    @SerializedName("k_items") val items: List<ItemsDoc>? = null,
    @SerializedName("k_cobros") val cobros: List<CobroItem>? = null
)

data class CobroItem(
    @SerializedName("k_vence") val vence: String,
    @SerializedName("k_refer") val refer: String,
    @SerializedName("k_iva") val iva: String,
    @SerializedName("k_docto") val docto: String,
    @SerializedName("k_saldom") val saldom: String,
    @SerializedName("k_descr") val descr: String,
    @SerializedName("k_monto_orig") val montoOrig: String,
    @SerializedName("k_saldo") val saldo: String,
    @SerializedName("k_monto") val monto: String,
    @SerializedName("k_naturaleza") val nat: String,
    @SerializedName("k_grupo") val grp: String,
    @SerializedName("k_tipo") val tipo: String,
    @SerializedName("k_folio") val folio: String,
    @SerializedName("k_fecha") val fecha: String
)

data class ItemsDoc(
    @SerializedName("k_parte") val kparte: String,
    @SerializedName("k_Q") val cant: String,
    @SerializedName("k_descr") val descri: String,
    @SerializedName("k_unidad") val uni: String,
    @SerializedName("k_precio") val precio: String,
    @SerializedName("k_monto") val monto: String,
    @SerializedName("k_coment") val coment: String? = null,
    @SerializedName("k_iva") val iva: String? = null,
    @SerializedName("k_ieps") val ieps: String? = null,
    @SerializedName("k_desc") val desc: String? = null,
    @SerializedName("k_refer") val ref: String? = null,
    @SerializedName("k_concepto") val concep: String? = null,
    @SerializedName("k_orden") val orden: String? = null,
    @SerializedName("k_itemAux") val itemAux: List<ItemsAuxiliar>? = null
)

data class ItemsAuxiliar(
    @SerializedName("k_serie") val serie: String? = null,
    @SerializedName("k_Q") val cant: String? = null,
    @SerializedName("k_caduca") val caduca: String? = null,
    @SerializedName("k_talla") val talla: String? = null,
    @SerializedName("k_modelo") val modelo: String? = null,
    @SerializedName("k_color") val color: String? = null,
    @SerializedName("k_ubicacion") val ubicacion: String? = null
)
