package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface Kdm1Dao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertaDocumento(docto: Kdm1Entity): Long

    @Query("SELECT count(monto) FROM kdm1_doctos WHERE cliente =:cli and staSinc = 'N'")
    suspend fun obtenerMovimiento(cli: String): Double?


    @Query("SELECT * FROM kdm1_doctos ")
    suspend fun obtenerMovimientos(): List<Kdm1Entity>

    @Query("DELETE FROM kdm1_doctos")
    suspend fun eliminarTodoMovimiento()
}