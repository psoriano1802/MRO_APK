package com.example.lacteos_flores.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lacteos_flores.R
import com.example.lacteos_flores.adapters.MenuAdapter
import com.example.lacteos_flores.adapters.MenuOptions
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.databinding.ActivityMainBinding
import com.example.lacteos_flores.utils.Prefs
import com.example.lacteos_flores.viewmodels.MenuViewModel
import kotlinx.coroutines.launch
import kotlin.jvm.java


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MenuViewModel

    private lateinit var db: AppDatabase
    private val allitems = listOf(
        MenuOptions("Jornada", R.drawable.ic_inventory,"JORNADA", JornadaActivity::class.java), //registra el inicio de labores y cargalos datos iniciales, catlogos y si hay cargas iniciales
        MenuOptions("Ventas", R.drawable.ic_orders,"VENTAS", VentasActivity::class.java), //registra el fin de laboresy termina el dia, no permite abrir dia hasta el dia siguiente
        MenuOptions("Cobranza", R.drawable.ic_reports,"CXC", CobrosActivity::class.java), //actualiza los datos catalogos, recargas
        MenuOptions("Devolucion", R.drawable.ic_settings,"DEV", VentasActivity::class.java) ,//
        MenuOptions("Descarga",R.drawable.ic_inventory,"DES", VentasActivity::class.java),
        MenuOptions("Gastos",R.drawable.ic_orders,"GAS", GastosActivity::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       viewModel = ViewModelProvider(this)[MenuViewModel::class.java]

        //abrimos la bd para poder consultar las existencias
        //inicializamos la base de datos
        db = AppDatabase.getDatabase(this)
        val userAct = Prefs(this).obtenerUsuario().first
       // System.out.println("userAct:"+userAct)
        //viewModel.obtenerPantallasPermitidas(userAct).observe(this) { pantallas ->
       //     val permitidas = pantallas.map { it.pantalla }
            val visibles= allitems

            val adapter = MenuAdapter(visibles){ accion ->
                /*val intent = Intent(this, accion)
                startActivity(intent)*/
                // Aquí recibimos el clic y validamos
                validarExistenciasYProceder(accion)
            }
            binding.recyclerViewMenu.layoutManager = GridLayoutManager(this, 2)
            binding.recyclerViewMenu.adapter = adapter

            // Configurar el escuchador de clics en el adaptador


        //}

    }
    private fun validarExistenciasYProceder(clase: Class<out AppCompatActivity>) {
        lifecycleScope.launch {
            // Consultamos el total de existencias
            val totalExistencia = db.existenciasDao().obtenerTodasExistencias()

            // Solo validamos si intenta entrar a VentasActivity
            if (clase == VentasActivity::class.java && totalExistencia <= 0) {
                Toast.makeText(this@MainActivity,
                    "No puedes continuar: No hay existencias en almacén. Sincroniza primero.",
                    Toast.LENGTH_LONG).show()
            } else {
                // Si es otra pantalla o hay existencias, permitimos el paso
                val intent = Intent(this@MainActivity, clase)
                startActivity(intent)
            }
        }
    }

}


