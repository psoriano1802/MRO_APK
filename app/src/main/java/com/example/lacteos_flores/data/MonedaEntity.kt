package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "moneda")//catalogos tabla queporeste desarrollo tomara tanto paridad y moneda
data class MonedaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val moneda: String,
    val fecha: String,
    val hora: String,
    val paridad: String
)