package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface Kdm1Dao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertaDocumento(docto: Kdm1Entity): Long

    @Query("SELECT * FROM kdm1_doctos WHERE id = :id and  suc = :suc and alm = :alm and gen = :gen and nat = :nat and grp = :grp and tip = :tip ")
    suspend fun obtenerCliente(id: Long, suc: String, alm: String, gen: String, nat: String, grp: String, tip: String): Kdm1Entity

    @Query("SELECT * FROM kdm1_doctos ")
    suspend fun obtenerTodosClientes(): List<Kdm1Entity>

    @Query("DELETE FROM kdm1_doctos")
    suspend fun eliminarTodoKdm1()
}