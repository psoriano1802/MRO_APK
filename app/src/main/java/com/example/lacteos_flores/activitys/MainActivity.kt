package com.example.lacteos_flores.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lacteos_flores.R
import com.example.lacteos_flores.adapters.MenuAdapter
import com.example.lacteos_flores.adapters.MenuOptions
import com.example.lacteos_flores.databinding.ActivityMainBinding
import com.example.lacteos_flores.utils.Prefs
import com.example.lacteos_flores.viewmodels.MenuViewModel
import kotlin.jvm.java


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MenuViewModel
    private val allitems = listOf(
        MenuOptions("Jornada", R.drawable.ic_inventory,"JORNADA", JornadaActivity::class.java), //registra el inicio de labores y cargalos datos iniciales, catlogos y si hay cargas iniciales
        MenuOptions("Ventas", R.drawable.ic_orders,"VENTAS", VentasActivity::class.java), //registra el fin de laboresy termina el dia, no permite abrir dia hasta el dia siguiente
        MenuOptions("Cobranza", R.drawable.ic_reports,"CXC", SolicitaRefaccionActivity::class.java), //actualiza los datos catalogos, recargas
        MenuOptions("Pantalla 4", R.drawable.ic_settings,"P2", JornadaActivity::class.java) //
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       viewModel = ViewModelProvider(this)[MenuViewModel::class.java]

        val userAct = Prefs(this).obtenerUsuario().first
       // System.out.println("userAct:"+userAct)
        //viewModel.obtenerPantallasPermitidas(userAct).observe(this) { pantallas ->
       //     val permitidas = pantallas.map { it.pantalla }
            val visibles= allitems

            val adapter = MenuAdapter(visibles){ accion ->
                val intent = Intent(this, accion)
                startActivity(intent)
            }
            binding.recyclerViewMenu.layoutManager = GridLayoutManager(this, 2)
            binding.recyclerViewMenu.adapter = adapter

            // Configurar el escuchador de clics en el adaptador


        //}

    }

}


