package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ListaPrecioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarListasPrecios(lista: List<ListaPreciosEntity>)

    @Query("SELECT * FROM listaprecios WHERE clave like :cve and listaid = :listaid ")
    suspend fun obtenerListado(cve: String, listaid: String): ListaPreciosEntity?

    //buscamos el productos y sus existencia total
    @Query("""
    SELECT p.clave, p.descripcion, p.cb, p.unidad, p.unidadalt, 
           COALESCE(l.precio, '0.0') as precio1, 
           p.precio2, p.precio3, p.precio4, p.iva, p.ieps, p.ubicaalm, 
           p.serie, p.lotesf, p.tmc, p.ubicacionn, 
           '' as pedimento, 
           SUM(CAST(COALESCE(e.existencias, '0') AS DOUBLE)) as existencia
    FROM productos p
    LEFT JOIN existencias e ON p.clave = e.clave
    LEFT JOIN listaprecios l ON p.clave = l.clave AND l.listaid = :listaid
    WHERE p.descripcion LIKE :filtro OR p.clave LIKE :filtro
    GROUP BY p.clave
""")
    suspend fun obtenerProductosConExistencia(filtro: String, listaid: String): List<ProductosEntity>



    @Query("SELECT * FROM listaprecios")
    suspend fun obtenerTodasListasPrecios(): List<ListaPreciosEntity>

    @Query("DELETE FROM listaprecios")
    suspend fun eliminarTodo()

}