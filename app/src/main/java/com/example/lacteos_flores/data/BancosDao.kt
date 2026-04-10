package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BancosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarBanco(banco: List<BancoEntity>)

    @Query("SELECT * FROM bancos")
    suspend fun obtenerBancos(): List<BancoEntity>

    @Query("DELETE FROM bancos")
    suspend fun eliminarTodo()

}