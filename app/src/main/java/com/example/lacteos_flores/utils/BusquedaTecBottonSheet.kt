package com.example.lacteos_flores.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lacteos_flores.adapters.ResultadoBuscarTecAdapter
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.ClientsDao
import com.example.lacteos_flores.data.ClientsEntity
import com.example.lacteos_flores.data.UsuarioDao
import com.example.lacteos_flores.databinding.DialogBuscaTecnicoBinding
import com.example.lacteos_flores.interfaz.ApiService
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.Login
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class BusquedaTecBottonSheet(
    private val onItemSelected: (ClientsEntity) -> Unit // Callback al seleccionar un resultado
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBuscaTecnicoBinding
    private lateinit var user: String
    private lateinit var pass: String
    private lateinit var prefs: Prefs
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBuscaTecnicoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = Prefs(requireContext())
        //obtenemos el usuario y la contraseña de las preferencias
        user = prefs.obtenerUsuario().first.toString()
        pass = prefs.obtenerUsuario().second.toString()
        //inicializamos la base de datos
        db = AppDatabase.getDatabase(requireContext())

        binding.btnBuscar.setOnClickListener {
            val texto = binding.editTextBusqueda.text.toString()
            if (texto.length < 3) {
                binding.editTextBusqueda.error = "Ingresa al menos 3 caracteres"
                return@setOnClickListener
            }
            buscar(texto)
        }
    }
    //funcion para hacer la busqueda de los activos en base a la opcion seleccionada
    private fun buscar(texto: String) {
        lifecycleScope.launch {
            try {
                //buscamos los clientes en la base de datos local con el metodo obtenerTodosClientes de la interfaz ClientsDao
                val clientes = db.clientsDao().obtenerTodosClientes(texto)

                if (clientes.isNotEmpty()) {
                    binding.recyclerResultadosTecnicos.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerResultadosTecnicos.adapter =
                        ResultadoBuscarTecAdapter(clientes) { seleccionado ->
                            onItemSelected(seleccionado)
                            dismiss()
                        }
                } else {
                    Toast.makeText(requireContext(), "Cliente no encontrado", Toast.LENGTH_SHORT).show()

                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show()
                println("Error en la búsqueda: ${e.message}")
            }
        }
    }
}
