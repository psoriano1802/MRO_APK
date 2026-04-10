package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DoctosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDocumentos(documentos: List<DoctosEntity>)

    @Query("DELETE FROM documentos")
    suspend fun eliminarTodo()

    @Query("SELECT * FROM documentos")
    suspend fun obtenerDocumentos(): List<DoctosEntity>

}