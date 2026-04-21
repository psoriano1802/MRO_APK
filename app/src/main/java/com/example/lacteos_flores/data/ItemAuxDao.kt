package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ItemAuxDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertaPartidasAux(partidasAux: List<ItemAuxEntity>):List<Long>

    @Query("SELECT * FROM itemAux WHERE iddoc = :id and  suc = :suc and alm = :alm and gen = :gen and nat = :nat and grp = :grp and tip = :tip ")
    suspend fun obtenerPartidasPorDocumentoAux(id: Long, suc: String, alm: String, gen: String, nat: String, grp: String, tip: String): List<ItemAuxEntity>

    @Query("SELECT * FROM itemAux ")
    suspend fun obtenerAllPartidasAux(): List<ItemAuxEntity>

    @Query("DELETE FROM itemAux")
    suspend fun eliminarTodoItemAux()
}