package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface GastoRegistradoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGasto(gasto: GastoRegistradoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGastos(gastos: List<GastoRegistradoEntity>)

    @Query("SELECT * FROM gastos_registrados WHERE sincronizado = 0")
    suspend fun obtenerGastosPendientes(): List<GastoRegistradoEntity>

    @Update
    suspend fun actualizarGasto(gasto: GastoRegistradoEntity)

    @Query("UPDATE gastos_registrados SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)

    @Query("SELECT * FROM gastos_registrados WHERE id = :id")
    suspend fun obtenerGastoPorId(id: Long): GastoRegistradoEntity?

    @Query("DELETE FROM gastos_registrados")
    suspend fun eliminarTodoGastosRegistrados()
}
