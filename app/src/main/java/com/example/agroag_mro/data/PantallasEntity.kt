package com.example.agroag_mro.data

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pantallas")
data class PantallasEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuario: String,
    val pantalla: String,
    val acceso: Boolean = false

)