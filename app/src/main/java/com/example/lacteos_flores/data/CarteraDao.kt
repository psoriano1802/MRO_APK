package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CarteraDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertaCartera(docto: List<CarteraEntity>)

    @Query("SELECT * FROM cartera where cli = :cli ")
    suspend fun obtenerMCarteras(cli: String): List<CarteraEntity>

    @Query("SELECT * FROM cartera where cli = :cli and dias < 0")
    suspend fun obtenerDocVence(cli: String): List<CarteraEntity>

    @Query("DELETE FROM cartera")
    suspend fun eliminarTodo()
}

