package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExistenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarExistencias(existencia: List<ExistenciaEntity>)

    @Query("SELECT * FROM existencias WHERE clave = :cve ")
    suspend fun obtenerExistencia(cve: String): List<ExistenciaEntity>?

    //consultar el total de existencias

    @Query("SELECT count(*) FROM existencias")
    suspend fun obtenerTodasExistencias(): Int

    @Query("DELETE FROM existencias")
    suspend fun eliminarTodo()

}