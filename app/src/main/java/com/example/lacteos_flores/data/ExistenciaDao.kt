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

    @Query("SELECT * FROM existencias WHERE clave = :cve AND auxiliar = :auxiliar LIMIT 1")
    suspend fun obtenerLoteEspecifico(cve: String, auxiliar: String): ExistenciaEntity?

    //consultar el total de existencias

    @Query("SELECT count(*) FROM existencias")
    suspend fun obtenerTodasExistencias(): Int

    @Query("SELECT * FROM existencias WHERE clave = :cve AND CAST(existencias AS DOUBLE) > 0 ORDER BY fecha ASC")
    suspend fun obtenerLotesDisponibles(cve: String): List<ExistenciaEntity>

    @Query("UPDATE existencias SET existencias = :nuevaExistencia WHERE clave = :cve AND auxiliar = :auxiliar")
    suspend fun actualizarExistencia(cve: String, auxiliar: String, nuevaExistencia: String)

    @Query("DELETE FROM existencias")
    suspend fun eliminarTodo()

    @Query("""
        SELECT p.clave, p.descripcion, p.cb, p.unidad, p.unidadalt, p.precio1, p.precio2, p.precio3, p.precio4, p.iva, p.ieps, p.ubicaalm, p.serie, p.lotesf, p.tmc, p.ubicacionn, p.pedimento, 
        SUM(CAST(e.existencias AS DOUBLE)) as existencia 
        FROM productos p 
        INNER JOIN existencias e ON p.clave = e.clave 
        GROUP BY p.clave 
        HAVING  SUM(CAST(e.existencias AS DOUBLE)) > 0
    """)
    suspend fun obtenerProductosConStock(): List<ProductosEntity>
}
