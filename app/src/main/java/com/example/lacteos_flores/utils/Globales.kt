package com.example.lacteos_flores.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.UsuarioDao
import com.example.lacteos_flores.data.UsuarioEntity
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.AltaDoctosRequest
import com.example.lacteos_flores.models.AltaDoctosResponse
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.PaquetesRequest
import com.example.lacteos_flores.models.itemsDoc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

//clase resultado generica
sealed class Result<out T>{
    data class Success<out T>(val data: T): Result<T>()
    data class Error(val exception: Exception): Result<Nothing>()
    object Empty: Result<Nothing>()
}

object Globales{
    private const val PREFS_NAME = "GlobalPrefs"
    private const val KEY_USUARIO = "usuario"
    private const val KEY_PASSWORD = "password"
    //private const val KEY_SUCURSAL = "sucursal"
    //private const val KEY_NOMBRE = "nombre"

    private var database: AppDatabase? = null


    private lateinit var prefs: SharedPreferences

    //iniciailzar
    fun init(context: Context){
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if(database == null){
            database = AppDatabase.getDatabase(context)
        }
    }

    //funcion para obtener la base de datos
    private fun getUsuarioDao(): UsuarioDao {
        return database?.usuarioDao()
            ?: throw IllegalStateException("Database not initialized")

    }
    //obtener usuario por id
    suspend fun obtenerUsuarioPorId(user: String): UsuarioEntity? {
        return getUsuarioDao().obtenerUsuario(user)
    }

    //variables persistentes
    var usuario: String?
        get() = prefs.getString(KEY_USUARIO, null)
        set(value) = prefs.edit().putString(KEY_USUARIO, value).apply()
    var password: String?
        get() = prefs.getString(KEY_PASSWORD, null)
        set(value) = prefs.edit().putString(KEY_PASSWORD, value).apply()

    //funciones

    //mensajes
    fun showToast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    //limpia datos
    fun clear(){
        prefs.edit().clear().apply()
    }

    //funcion para consumo de ws
    suspend fun <T> consumeWS(
        call: suspend () -> Response<T>
    ): Result<T>{
        return withContext(Dispatchers.IO){
            try {
               val response = call()
                if(response.isSuccessful){
                    val body = response.body()
                    if(body != null){
                        Result.Success(body)
                    }else{
                        Result.Empty
                    }
                }else{
                    Result.Error(Exception("Error HTTP: ${response.code()}"))
                }
            }catch (e: Exception){
                Result.Error(e)
            }
        }
    }

    //funcion para obtenes documentos desde el ws
    suspend fun obtenerDocumentos(): Result<Any> {
        return consumeWS {
            val request = LoginRequest(Login(usuario.toString(), password.toString()))
            RetrofitClient.apiService.getClientes(request)

        }
    }



    suspend fun sendDocto(suc: String,alm: String,gen: String,nat: String,grp: String,tip: String,fec: String,mon: String,pari: String,ref: String,coment: String,soli: String,monto: String,fe: String,te: String,sal: String,proy: String,depto: String,items: List<itemsDoc>): Result<AltaDoctosResponse> {
        return consumeWS {
            val login = Login(usuario.toString(), password.toString())
            val request = AltaDoctosRequest(login,suc,alm,gen,nat,grp,tip,fec,mon,pari,ref,coment,null,null,null,null,null,null,null,null,null,soli,monto,fe,te,sal,proy,depto,items)
            println("requestalta:"+request)
            RetrofitClient.apiService.sendDoctos(request)
        }
    }


}