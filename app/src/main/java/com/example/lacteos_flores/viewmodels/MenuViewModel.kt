package com.example.lacteos_flores.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.PantallasEntity

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val pantallasDao = AppDatabase.getDatabase(application).usuarioDao()

    fun obtenerPantallasPermitidas(usuario: String): LiveData<List<PantallasEntity>> {
        return liveData {
            val resultado = pantallasDao.obtenerPantallas(usuario)
            System.out.println("Opciones permitidas: " + resultado)
            emit(resultado)
        }
    }
}
