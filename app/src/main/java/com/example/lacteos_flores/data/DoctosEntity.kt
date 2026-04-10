package com.example.lacteos_flores.data


import androidx.room.Entity
import androidx.room.PrimaryKey

   @Entity(tableName = "documentos")//catalogos
    data class DoctosEntity(
       @PrimaryKey (autoGenerate = true)
        val id: Long = 0,
        val gen: String,
        val nat: String,
        val grp: String,
        val tipo: String,
        val descripcion: String,
        val isr: String,
        val iva: String,
        val retenido: String
    )

