package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClientsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarClientes(cliente: List<ClientsEntity>)

    @Query("SELECT * FROM clientes WHERE clave = :cve ")
    suspend fun obtenerCliente(cve: String): ClientsEntity?

    @Query("SELECT * FROM clientes where clave like '%' || :cve || '%' or nombre like '%' || :cve || '%'")
    suspend fun obtenerTodosClientes(cve: String): List<ClientsEntity>

    @Query("UPDATE clientes SET limcre = :nuevoLimite WHERE clave = :cve")
    suspend fun actualizarLimiteCredito(cve: String, nuevoLimite: String)

    @Query("DELETE FROM clientes")
    suspend fun eliminarTodo()

}