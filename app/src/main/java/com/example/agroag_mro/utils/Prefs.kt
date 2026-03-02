package com.example.agroag_mro.utils

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val PREFS_NAME = "com.example.baseloginapp.prefs"
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    //guardamos el usuario con clave, contraseña, nombre y sucursal
    fun guardarUsuario(clave: String, contraseña:String, sucursal: String) {
        sharedPreferences.edit().putString("clave", clave).apply()
        sharedPreferences.edit().putString("contraseña", contraseña).apply()
        sharedPreferences.edit().putString("sucursal", sucursal).apply()
    }

    //obtenemos el usuario con clave, contraseña, nombre y sucursal
    fun obtenerUsuario(): Pair<String, String> {
        val clave = sharedPreferences.getString("clave", "") ?: ""
        val pass = sharedPreferences.getString("contraseña", "") ?: ""
        return Pair(clave, pass)
    }



    //eliminamos el usuario
    fun eliminarUsuario() {
        sharedPreferences.edit().remove("clave").apply()
        sharedPreferences.edit().remove("nombre").apply()
        sharedPreferences.edit().remove("sucursal").apply()
    }

    // Elimina una clave
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    // Limpia todo
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
