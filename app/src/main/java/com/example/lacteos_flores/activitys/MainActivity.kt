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
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.Validadia
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MenuViewModel

    private lateinit var db: AppDatabase
    private var usuario: String? = null
    private var pass: String? = null
    private var esJornadaActiva = false
    private var esJornadaFinalizada = false

    private val allitems = listOf(
        MenuOptions("Jornada", R.drawable.ic_inventory,"JORNADA", JornadaActivity::class.java), //registra el inicio de labores y cargalos datos iniciales, catlogos y si hay cargas iniciales
        MenuOptions("Ventas", R.drawable.ic_orders,"VENTAS", VentasActivity::class.java), //registra el fin de laboresy termina el dia, no permite abrir dia hasta el dia siguiente
        MenuOptions("Cobranza", R.drawable.ic_reports,"CXC", CobrosActivity::class.java), //actualiza los datos catalogos, recargas
        MenuOptions("Devolucion", R.drawable.ic_settings,"DEV", DevolucionesActivity::class.java) ,//
        MenuOptions("Descarga",R.drawable.ic_inventory,"DES", DescargasActivity::class.java),
        MenuOptions("Gastos",R.drawable.ic_orders,"GAS", GastosActivity::class.java),
        MenuOptions("Sincronizar", R.drawable.ic_settings, "SYNC", SincronizarDatosActivity::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       viewModel = ViewModelProvider(this)[MenuViewModel::class.java]

        //abrimos la bd para poder consultar las existencias
        //inicializamos la base de datos
        db = AppDatabase.getDatabase(this)
        val userData = Prefs(this).obtenerUsuario()
        usuario = userData.first
        pass = userData.second

        validarEstadoJornada()

        val visibles= allitems

        val adapter = MenuAdapter(visibles){ accion ->
            validarJornadaYProceder(accion)
        }
            binding.recyclerViewMenu.layoutManager = GridLayoutManager(this, 2)
            binding.recyclerViewMenu.adapter = adapter

            // Configurar el escuchador de clics en el adaptador


        //}

    }

    private fun validarEstadoJornada() {
        lifecycleScope.launch {
            try {
                val login = Login(usuario.toString(), pass.toString())
                val response = RetrofitClient.apiService.validaDia(LoginRequest(login))
                if (response.isSuccessful) {
                    val validaDia = response.body()?.ValidaDiaResponse?.getOrNull(0)
                    if (validaDia != null) {
                        procesarValidacion(validaDia)
                    }
                }
            } catch (e: Exception) {
                // Silencioso o log en consola para no interrumpir el flujo principal del menú
                System.out.println("Error al validar jornada: ${e.message}")
            }
        }
    }

    private fun procesarValidacion(valida: Validadia) {
        val fechaInicioStr = valida.fecini
        val fechaTerminaStr = valida.fecter

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaHoy = sdf.format(System.currentTimeMillis())

        if (fechaInicioStr == null || fechaInicioStr == "-") {
            println("primer if")
            esJornadaActiva = false
            esJornadaFinalizada = false
        } else if (fechaTerminaStr == null || fechaTerminaStr == "-") {
            println("aegundo if")
            esJornadaActiva = true
            esJornadaFinalizada = false
        } else {
            println("tercer if")
            val soloFechaTermina = fechaTerminaStr.split(" ")[0]
            if (soloFechaTermina == fechaHoy) {
                esJornadaActiva = false
                esJornadaFinalizada = true
            } else {
                esJornadaActiva = false
                esJornadaFinalizada = false
            }
        }
    }

    private fun validarJornadaYProceder(clase: Class<out AppCompatActivity>) {
        if (clase == JornadaActivity::class.java) {
            startActivity(Intent(this, clase))
            return
        }

        when {
            esJornadaFinalizada -> {
                Toast.makeText(this, "La jornada de hoy ya ha sido finalizada.", Toast.LENGTH_LONG).show()
            }
            !esJornadaActiva -> {
                Toast.makeText(this, "Debes iniciar la jornada antes de realizar operaciones.", Toast.LENGTH_LONG).show()
            }
            else -> {
                if (clase == VentasActivity::class.java) {
                    lifecycleScope.launch {
                        val totalExistencia = db.existenciasDao().obtenerTodasExistencias()
                        if (totalExistencia <= 0) {
                            Toast.makeText(this@MainActivity, "No hay existencias. Sincroniza primero.", Toast.LENGTH_LONG).show()
                        } else {
                            startActivity(Intent(this@MainActivity, clase))
                        }
                    }
                } else {
                    startActivity(Intent(this, clase))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        validarEstadoJornada()
    }

}


