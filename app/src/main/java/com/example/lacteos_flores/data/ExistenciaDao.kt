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

    @Query("SELECT * FROM existencias WHERE clave = :cve AND CAST(existencias AS DOUBLE) > 0 ORDER BY fecha ASC")
    suspend fun obtenerLotesDisponibles(cve: String): List<ExistenciaEntity>

    @Query("UPDATE existencias SET existencias = :nuevaExistencia WHERE clave = :cve AND auxiliar = :auxiliar")
    suspend fun actualizarExistencia(cve: String, auxiliar: String, nuevaExistencia: String)

    @Query("DELETE FROM existencias")
    suspend fun eliminarTodo()

}