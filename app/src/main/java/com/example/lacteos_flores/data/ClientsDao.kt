package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClientsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cliente: ClientsEntity)

    @Query("SELECT * FROM clientes WHERE clave = :cve ")
    suspend fun obtenerCliente(cve: String): ClientsEntity?

    @Query("SELECT * FROM clientes")
    suspend fun obtenerTodosClientes(): List<ClientsEntity>

    @Query("DELETE FROM clientes")
    suspend fun eliminarTodo()

}