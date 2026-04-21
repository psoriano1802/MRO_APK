package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

//tabla de encabezados de documentos para las ventas
@Entity(tableName = "kdm1_doctos")//catalogos
data class Kdm1Entity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val suc: String,
    val alm: String,
    val gen: String,
    val nat: String,
    val grp: String,
    val tip: String,
    val fecha: String,
    val cliente: String,
    val moneda: String,
    val pari: String,
    val rfc: String,
    val venc: String,
    val condi: String,
    val agent: String,
    val lati: String,
    val long: String,

)
