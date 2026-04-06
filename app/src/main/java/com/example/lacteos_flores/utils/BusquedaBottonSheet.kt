package com.example.lacteos_flores.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lacteos_flores.adapters.ResultadoAdapter
import com.example.lacteos_flores.databinding.DialogBusquedaBinding
import com.example.lacteos_flores.interfaz.ApiService
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.ubicacionRequest
import com.example.lacteos_flores.models.itemsDoc
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.TiposActivoResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class BusquedaBottomSheet(
    private val onItemSelected: (itemsDoc) -> Unit // Callback al seleccionar un resultado
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBusquedaBinding
    private lateinit var apiService: ApiService
    private lateinit var user: String
    private lateinit var pass: String
    private lateinit var prefs: Prefs
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBusquedaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.apiService
        prefs = Prefs(requireContext())
        //obtenemos el usuario y la contraseña de las preferencias
        user = prefs.obtenerUsuario().first.toString()
        pass = prefs.obtenerUsuario().second.toString()
        cargarOpciones()

        binding.btnBuscar.setOnClickListener {
            val texto = binding.editTextBusqueda.text.toString()
            if (texto.length < 3) {
                binding.editTextBusqueda.error = "Ingresa al menos 3 caracteres"
                return@setOnClickListener
            }

            val opcion = binding.spinnerOpciones.selectedItem.toString()
            //obtenermosel primer elemento del spinner
            val opcionSeleccionada = opcion.split(" - ").first()
            buscar(opcionSeleccionada, texto)
        }
    }

    private fun cargarOpciones() {

        lifecycleScope.launch {
            try {

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar opciones", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupTiposActivo(response: TiposActivoResponse ) {
        val tiposActivo = response.ResponseTiposActivos?.filter { it.cve != null  }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tiposActivo?.map { "${it.cve} - ${it.name}" } ?: emptyList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOpciones.adapter = adapter
    }
    //funcion para hacer la busqueda de los activos en base a la opcion seleccionada
    private fun buscar(opcion: String, texto: String) {
        lifecycleScope.launch {
            try {

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
