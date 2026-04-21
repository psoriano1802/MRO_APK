package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GastosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGasto(gastos: List<GastosEntity>)

    @Query("SELECT * FROM gastos WHERE clave = :cve ")
    suspend fun obtenerGastos(cve: String): GastosEntity?

    @Query("SELECT * FROM gastos")
    suspend fun obtenerTodosGastos(): List<GastosEntity>

    @Query("DELETE FROM gastos")
    suspend fun eliminarTodo()

}


