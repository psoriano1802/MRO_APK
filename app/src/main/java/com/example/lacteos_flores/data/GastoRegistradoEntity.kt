package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos_registrados")
data class GastoRegistradoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipoGasto: String,
    val monto: Double,
    val comentario: String,
    val fecha: String,
    val usuario: String,
    val sucursal: String? = null,
    val almacen: String? = null,
    var sincronizado: Boolean = false
)
