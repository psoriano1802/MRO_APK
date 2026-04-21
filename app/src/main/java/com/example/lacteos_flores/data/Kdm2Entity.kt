package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kdm2_partidas",primaryKeys = ["iddoc","suc", "alm", "gen", "nat", "grp", "tip", "partida"])//tablas para partidas de los documentos generados
data class Kdm2Entity(
    val iddoc: Long,
    val suc: String,
    val alm: String,
    val gen: String,
    val nat: String,
    val grp: String,
    val tip: String,
    val partida: String,
    val producto: String,
    val cantidad: String,
    val descrip: String,
    val unidad: String,
    val precio: String,
    val importe: String,
    val iva: String
)