package com.example.lacteos_flores.data

import android.R
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(tableName = "cartera", primaryKeys = ["cli", "docto"])
data class CarteraEntity(
    val cli: String,
    val docto: String,
    val monto: String,
    val saldo: String,
    val fecha: String,
    val credito: String,
    val abono: String,
    val dias: String
)