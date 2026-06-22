package com.example.lacteos_flores.data

import androidx.room.*

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuario where  LOWER(usuario) = LOWER(:usuario)")
    suspend fun obtenerUsuario(usuario: String): UsuarioEntity?

    @Query("SELECT * FROM usuario LIMIT 1")
    suspend fun obtenerCualquierUsuario(): UsuarioEntity?
    //obtenemos la sucursal del usuario

    //guardamos pantallas
    /*@Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPantallas(pantallas: PantallasEntity)
    //consultamos pantallas
    @Query("SELECT * FROM pantallas WHERE usuario = :usuario")
    suspend fun obtenerPantallas(usuario: String): List<PantallasEntity>
    //eliminamos las pantallas del usuario
    @Query("DELETE FROM pantallas WHERE usuario = :usuario")
    suspend fun eliminarPantallas(usuario: String)*/


}
