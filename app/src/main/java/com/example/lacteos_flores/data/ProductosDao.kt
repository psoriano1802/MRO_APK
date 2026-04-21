package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductos(producto: List<ProductosEntity>)

    @Query("SELECT * FROM productos WHERE clave = :cve ")
    suspend fun obtenerProducto(cve: String): ProductosEntity?

    @Query("SELECT * FROM productos where descripcion like :text or clave like :text")
    suspend fun obtenerTodosProductos(text: String): List<ProductosEntity>

    @Query("DELETE FROM productos")
    suspend fun eliminarTodo()

}