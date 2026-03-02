package com.example.agroag_mro.models

import com.google.gson.annotations.SerializedName

data class AltaDoctosRequest (
    val login: Login,
    @SerializedName("k_sucursal") val suc: String,
    @SerializedName("k_almacen") val alm: String,
    @SerializedName("k_genero") val gen: String,
    @SerializedName("k_naturaleza") val nat: String,
    @SerializedName("k_grupo") val grp: String,
    @SerializedName("k_tipo") val tipo: String,
    @SerializedName("k_fecha") val fecha: String,
    @SerializedName("k_moneda") val moneda: String,
    @SerializedName("k_paridad") val paridad: String,
    @SerializedName("k_refer") val ref: String,
    @SerializedName("k_coment") val comenta: String,
    @SerializedName("k_63") val k63: String?= null,
    @SerializedName("k_81") val k81: String?= null,
    @SerializedName("k_82") val k82: String?= null,
    @SerializedName("k_83") val k83: String?= null,
    @SerializedName("k_84") val k84: String?= null,
    @SerializedName("k_85") val k85: String?= null,
    @SerializedName("k_86") val k86: String?= null,
    @SerializedName("k_88") val k88: String?= null,
    @SerializedName("k_89") val k89: String?= null,
    @SerializedName("k_solicita") val solicita: String,
    @SerializedName("k_monto") val monto: String,
    @SerializedName("k_vence") val fentrega: String,
    @SerializedName("k_venceHr") val timentrega: String,
    @SerializedName("k_saldo") val saldo: String,
    @SerializedName("k_proyecto") val proyecto: String,
    @SerializedName("k_depto") val depto: String,
    @SerializedName("k_items") val items: List<itemsDoc>
)

data class itemsDoc(
    @SerializedName("k_parte") val kparte: String,
    @SerializedName("k_Q") val cant: String,
    @SerializedName("k_descr") val descri: String,
    @SerializedName("k_unidad") val uni: String,
    @SerializedName("k_precio") val precio: String,
    @SerializedName("k_refer") val ref: String,
    @SerializedName("k_concepto") val concep: String,
    @SerializedName("k_monto") val monto: String,
    @SerializedName("k_orden") val orden: String?= null,
    @SerializedName("k_itemAux") val itemaux: List<itemsAuxiliar>?= null

)

data class itemsAuxiliar(
    @SerializedName("k_serie") val serie: String?= null,
    @SerializedName("k_Q") val cant: String?= null ,
    @SerializedName("k_caduca") val caduca: String?= null,
    @SerializedName("k_talla") val talla: String?= null,
    @SerializedName("k_modelo") val model: String?= null,
    @SerializedName("k_color") val color: String?= null,
    @SerializedName("k_ubicacion") val ubic: String?= null

)