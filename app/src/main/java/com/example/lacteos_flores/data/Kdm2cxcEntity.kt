package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kdm2cxc")
data class Kdm2cxcEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val iddoc: Long, // Relación con kdm1_doctos.id
    val doctoAfectado: String,
    val saldoAnt: String,
    val abono: String,
    val fecha: String,
    val descri: String,
    val moneda: String,
    val montoDocto: String,
    val pari: String,
    val referencia: String
)
