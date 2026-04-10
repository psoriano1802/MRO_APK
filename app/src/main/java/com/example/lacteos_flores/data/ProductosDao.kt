package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: ProductosEntity)

    @Query("SELECT * FROM productos WHERE clave = :cve ")
    suspend fun obtenerProducto(cve: String): ProductosEntity?

    @Query("SELECT * FROM productos")
    suspend fun obtenerTodosProductos(): List<ProductosEntity>?

    @Query("DELETE FROM productos")
    suspend fun eliminarTodo()

}