package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface Kdm2Dao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertaPartidas(partidas: List<Kdm2Entity>):List<Long>

    @Query("SELECT * FROM kdm2_partidas WHERE iddoc = :id and  suc = :suc and alm = :alm and gen = :gen and nat = :nat and grp = :grp and tip = :tip ")
    suspend fun obtenerPartidasPorDocumento(id: Long, suc: String, alm: String, gen: String, nat: String, grp: String, tip: String): List<Kdm2Entity>

    @Query("SELECT * FROM kdm2_partidas ")
    suspend fun obtenerAllPartidas(): List<Kdm2Entity>

    @Query("DELETE FROM kdm2_partidas")
    suspend fun eliminarTodoKdm2()
}