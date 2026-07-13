package com.example.agroag_mro.activitys

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.agroag_mro.R
import com.example.agroag_mro.adapters.MenuAdapter
import com.example.agroag_mro.adapters.MenuOptions
import com.example.agroag_mro.databinding.ActivityMainBinding
import com.example.agroag_mro.utils.Globales
import com.example.agroag_mro.utils.Prefs
import com.example.agroag_mro.viewmodels.MenuViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

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
            binding.recyclerViewMenu.layoutManager = GridLayoutManager(this, 1)
            binding.recyclerViewMenu.adapter = adapter

            // Configurar el escuchador de clics en el adaptador
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Desea salir y cerrar sesion ?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Sí") { _, _ ->
                        logout()
                    }
                    .show()
            }
        })

    }

    private fun logout() {
        // Guardamos el estado del servidor de pruebas antes de limpiar
        val isTest = Globales.isTestServer
        
        // Limpiamos datos de sesión
        Prefs(this).clear()
        Globales.clear()
        
        // Restauramos el servidor de pruebas para que no se pierda la configuración
        Globales.isTestServer = isTest
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}


