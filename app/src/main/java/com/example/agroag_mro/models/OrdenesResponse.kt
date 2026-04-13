package com.example.agroag_mro.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class OrdenesResponse (
    val ResponseOrdenesAsig: List<OrdenItem>
)

data class OrdenItem(
    val ok:String? = null,
    @SerializedName("Folio")  val folio:String? = null,
    @SerializedName("CveActivo")  val activos: String?= null,
    @SerializedName("NombreActivo")  val nomAct: String?= null,
    @SerializedName("SucOrd")  val suc: String?= null,
    @SerializedName("TipOrd")  val tipo: String?= null,
    @SerializedName("fAlta")  val falta: String?= null,
    @SerializedName("fInicioOrd")  val finicio: String?= null,
    @SerializedName("CCOrd")  val cc: String?= null,
    @SerializedName("genOrd")  val gen: String?= null,
    @SerializedName("natOrd")  val nat: String?= null,
    @SerializedName("gpoOrd")  val grp: String?= null,
    @SerializedName("tipOrd")  val tip: String?= null,
    @SerializedName("folOrd")  val folOrd: String?= null,
    @SerializedName("paquete")  val paq: String?= null,
    @SerializedName("Err")  val err: String?= null


): Serializable

//para tomar las ordenes del actuvo por usuarios
data class OrdenesResponseActivo (
    val ResponseBusActUsr: List<OrdenItemActivo>
)
data class OrdenItemActivo(
    val ok:String? = null,
    @SerializedName("OrdenM")  val orden:String? = null,
    @SerializedName("Tipo")  val tipo: String?= null,
    @SerializedName("ABC")  val abc: String?= null,
    @SerializedName("Activo")  val activo: String?= null,
    @SerializedName("NomActivo")  val nactivo: String?= null,
    @SerializedName("CvePaque")  val paque: String?= null,
    @SerializedName("NomPaque")  val npaque: String?= null,
    @SerializedName("Observaciones")  val obser: String?= null,

)

