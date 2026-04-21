package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itemAux",primaryKeys = ["iddoc","suc", "alm", "gen", "nat", "grp", "tip", "partida","producto","auxiliar"])//control auxiliar para las partidas de los documentos generados
data class ItemAuxEntity(
    val iddoc: Long,
    val suc: String,
    val alm: String,
    val gen: String,
    val nat: String,
    val grp: String,
    val tip: String,
    val auxiliar: String,
    val partida: String,
    val producto: String,
    val cantidad: String
)