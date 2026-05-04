package com.example.agroag_mro.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.agroag_mro.R
import com.example.agroag_mro.adapters.MenuAdapter
import com.example.agroag_mro.adapters.MenuOptions
import com.example.agroag_mro.databinding.ActivityMainBinding
import com.example.agroag_mro.utils.Prefs
import com.example.agroag_mro.viewmodels.MenuViewModel
import kotlin.jvm.java


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MenuViewModel
    private val allitems = listOf(
        MenuOptions("Reporte de falla", R.drawable.ic_inventory,"FALLA", ReporteFallaActivity::class.java),
        MenuOptions("Ordenes Asignadas", R.drawable.ic_orders,"ASIGNADAS", ListaOrdenesActivity::class.java),
       // MenuOptions("Solicita Refacciones", R.drawable.ic_reports,"REFACCIONES", SolicitaRefaccionActivity::class.java),
       // MenuOptions("Mano de Obra", R.drawable.ic_settings,"MANOOBRA", ManoObraActivity::class.java),
        MenuOptions("Valida Orden", R.drawable.ic_settings,"VALIDAORDEN", ValidaOrdenActivity::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       viewModel = ViewModelProvider(this)[MenuViewModel::class.java]

        val userAct = Prefs(this).obtenerUsuario().first
        //System.out.println("userAct:"+userAct)
        viewModel.obtenerPantallasPermitidas(userAct).observe(this) { pantallas ->
            val permitidas = pantallas.map { it.pantalla }
            val visibles= allitems.filter { it.clave in permitidas  }
            val adapter = MenuAdapter(visibles){ accion ->
                val intent = Intent(this, accion)
                startActivity(intent)
            }
            binding.recyclerViewMenu.layoutManager = GridLayoutManager(this, 2)
            binding.recyclerViewMenu.adapter = adapter

            // Configurar el escuchador de clics en el adaptador
        }

    }

}


