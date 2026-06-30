package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ExistenciaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarExistencias(existencia: List<ExistenciaEntity>)

    @Query("SELECT * FROM existencias WHERE clave = :cve ")
    suspend fun obtenerExistencia(cve: String): List<ExistenciaEntity>?

    @Query("SELECT * FROM existencias WHERE clave = :cve AND auxiliar = :auxiliar LIMIT 1")
    suspend fun obtenerLoteEspecifico(cve: String, auxiliar: String): ExistenciaEntity?

    //consultar el total de existencias

    @Query("SELECT count(existencias) FROM existencias")
    suspend fun obtenerTodasExistencias(): Int

    @Query("SELECT * FROM existencias WHERE clave = :cve AND CAST(existencias AS DOUBLE) > 0 ORDER BY fecha ASC")
    suspend fun obtenerLotesDisponibles(cve: String): List<ExistenciaEntity>

    @Query("UPDATE existencias SET existencias = :nuevaExistencia WHERE clave = :cve AND auxiliar = :auxiliar AND talla = :ta AND modelo = :mod AND color = :colo")
    suspend fun actualizarExistencia(cve: String, auxiliar: String, ta: String, mod: String, colo: String, nuevaExistencia: String)

    @Query("UPDATE existencias SET existencias = :nuevaExistencia WHERE clave = :cve AND auxiliar = :auxiliar AND talla = :ta AND modelo = :mod AND color = :colo")
    suspend fun actualizarExistAux(cve: String, auxiliar: String, nuevaExistencia: String, ta: String, mod: String, colo: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarExistencia(existencia: ExistenciaEntity): Long
    @Query("""
    UPDATE existencias
    SET existencias = CAST(existencias AS INTEGER) + CAST(:cantidad AS INTEGER)
    WHERE clave = :cve
      AND auxiliar = :auxiliar
      AND talla = :ta
      AND modelo = :mod
      AND color = :colo
""")
    suspend fun sumarExistencia(
        cve: String,
        auxiliar: String,
        cantidad: String,
        ta: String,
        mod: String,
        colo: String
    ): Int

    @Transaction
    suspend fun sumarOInsertar(existencia: ExistenciaEntity) {

        val actualizadas = sumarExistencia(
            existencia.clave,
            existencia.auxiliar,
            existencia.existencias,
            existencia.talla,
            existencia.modelo,
            existencia.color
        )

        if (actualizadas == 0) {
            insertarExistencia(existencia)
        }
    }
    @Query("DELETE FROM existencias")
    suspend fun eliminarTodo()

    data class StockTMC(
        val clave: String,
        val descripcion: String,
        val unidad: String,
        val existencia: Double,
        val talla: String,
        val modelo: String,
        val color: String
    )

    @Query("""
        SELECT e.clave, p.descripcion, p.unidad, 
        SUM(CAST(e.existencias AS DOUBLE)) as existencia,
        e.talla, e.modelo, e.color
        FROM existencias e
        INNER JOIN productos p ON e.clave = p.clave
        GROUP BY e.clave, e.talla, e.modelo, e.color
        HAVING SUM(CAST(e.existencias AS DOUBLE)) > 0
    """)
    suspend fun obtenerStockAgrupadoTMC(): List<StockTMC>

    @Query("""
        SELECT p.clave, p.descripcion, p.cb, p.unidad, p.unidadalt, p.precio1, p.precio2, p.precio3, p.precio4, p.iva, p.ieps, p.ubicaalm, p.serie, p.lotesf, p.tmc, p.ubicacionn, p.pedimento, 
        SUM(CAST(e.existencias AS DOUBLE)) as existencia 
        FROM productos p 
        INNER JOIN existencias e ON p.clave = e.clave 
        GROUP BY p.clave 
        HAVING  SUM(CAST(e.existencias AS DOUBLE)) > 0
    """)
    suspend fun obtenerProductosConStock(): List<ProductosEntity>

    @Query("SELECT DISTINCT talla FROM existencias WHERE clave = :cve AND talla != '-' AND talla != ''")
    suspend fun obtenerTallasPorProducto(cve: String): List<String>

    @Query("SELECT DISTINCT modelo FROM existencias WHERE clave = :cve AND modelo != '-' AND modelo != ''")
    suspend fun obtenerModelosPorProducto(cve: String): List<String>

    @Query("SELECT DISTINCT color FROM existencias WHERE clave = :cve AND color != '-' AND color != ''")
    suspend fun obtenerColoresPorProducto(cve: String): List<String>

    //para devoluciones
    @Query("SELECT DISTINCT clave FROM talla_aux WHERE clave != '-'")
    suspend fun obtenerTallasPorProductoDev(): List<String>

    @Query("SELECT DISTINCT clave FROM modelo_aux WHERE clave != '-' ")
    suspend fun obtenerModelosPorProductoDev(): List<String>

    @Query("SELECT DISTINCT clave FROM color_aux WHERE clave != '-'")
    suspend fun obtenerColoresPorProductoDev(): List<String>
}
