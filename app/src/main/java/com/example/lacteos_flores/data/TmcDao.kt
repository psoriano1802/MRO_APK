package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TmcDao {
    // Tallas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTallas(tallas: List<TallaAuxEntity>)

    @Query("SELECT * FROM talla_aux")
    suspend fun obtenerTallas(): List<TallaAuxEntity>

    @Query("DELETE FROM talla_aux")
    suspend fun eliminarTallas()

    // Modelos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarModelos(modelos: List<ModeloAuxEntity>)

    @Query("SELECT * FROM modelo_aux")
    suspend fun obtenerModelos(): List<ModeloAuxEntity>

    @Query("DELETE FROM modelo_aux")
    suspend fun eliminarModelos()

    // color
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarColors(colors: List<ColorAuxEntity>)

    @Query("SELECT * FROM color_aux")
    suspend fun obtenerColors(): List<ColorAuxEntity>

    @Query("DELETE FROM color_aux")
    suspend fun eliminarColors()
}
