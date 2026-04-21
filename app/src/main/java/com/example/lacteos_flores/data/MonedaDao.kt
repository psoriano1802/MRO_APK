package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MonedaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(moneda: List<MonedaEntity>)

    @Query("SELECT * FROM moneda")
    suspend fun obtenerMonedas(): List<MonedaEntity>

    @Query("DELETE FROM moneda")
    suspend fun eliminarTodo()

}