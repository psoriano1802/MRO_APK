package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface Kdm2cxcDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPartida(partida: Kdm2cxcEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPartidas(partidas: List<Kdm2cxcEntity>)

    @Query("SELECT * FROM kdm2cxc WHERE iddoc = :iddoc")
    suspend fun obtenerPartidasPorDoc(iddoc: Long): List<Kdm2cxcEntity>

    @Query("DELETE FROM kdm2cxc")
    suspend fun eliminarTodoKdm2cxc()
}
