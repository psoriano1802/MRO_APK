package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "productos")//catalogos general de productos(lista del productos)
data class ProductosEntity(

    @PrimaryKey val clave: String,
    val descripcion: String,
    val cb: String,
    val unidad: String,
    val unidadalt: String,
    val precio1: String,
    val precio2: String,
    val precio3: String,
    val precio4: String,
    val iva: String,
    val ieps: String,
    val ubicaalm: String,
    val serie: String,
    val lotesf: String,
    val tmc: String,
    val ubicacionn: String,
    val pedimento: String,
    val existencia: Double
)

