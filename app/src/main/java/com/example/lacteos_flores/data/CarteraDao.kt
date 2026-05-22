package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CarteraDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertaCartera(docto: List<CarteraEntity>)

    @Query("SELECT * FROM cartera where cli = :cli AND CAST(saldo AS DOUBLE) > 0")
    suspend fun obtenerMCarteras(cli: String): List<CarteraEntity>

    @Query("UPDATE cartera SET saldo = :nuevoSaldo WHERE cli = :cli AND docto = :docto")
    suspend fun actualizarSaldo(cli: String, docto: String, nuevoSaldo: String)

    @Query("DELETE FROM cartera where cli = :cli and docto = :docto")
    suspend fun eliminarDocumento(cli: String, docto: String)

    @Query("SELECT * FROM cartera where cli = :cli and dias < 0")
    suspend fun obtenerDocVence(cli: String): List<CarteraEntity>

    @Query("DELETE FROM cartera")
    suspend fun eliminarTodo()
}

