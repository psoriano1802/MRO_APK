package com.example.agroag_mro.utils

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agroag_mro.adapters.ResultadoAdapter
import com.example.agroag_mro.adapters.ResultadoBuscarTecAdapter
import com.example.agroag_mro.databinding.DialogBuscaTecnicoBinding
import com.example.agroag_mro.databinding.DialogBusquedaBinding
import com.example.agroag_mro.interfaz.ApiService
import com.example.agroag_mro.interfaz.RetrofitClient
import com.example.agroag_mro.models.ActivoRequest
import com.example.agroag_mro.models.Activos
import com.example.agroag_mro.models.ActivosResponse
import com.example.agroag_mro.models.Login
import com.example.agroag_mro.models.LoginRequest
import com.example.agroag_mro.models.TiposActivoResponse
import com.example.agroag_mro.models.itemTecnico
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class BusquedaTecBottonSheet(
    private val onItemSelected: (itemTecnico) -> Unit // Callback al seleccionar un resultado
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBuscaTecnicoBinding
    private lateinit var apiService: ApiService
    private lateinit var user: String
    private lateinit var pass: String
    private lateinit var prefs: Prefs
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBuscaTecnicoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.apiService
        prefs = Prefs(requireContext())
        //obtenemos el usuario y la contraseña de las preferencias
        user = prefs.obtenerUsuario().first.toString()
        pass = prefs.obtenerUsuario().second.toString()

        binding.btnBuscar.setOnClickListener {
            val texto = binding.editTextBusqueda.text.toString()
            if (texto.length < 0) {
                binding.editTextBusqueda.error = "Ingresa al menos 1 caracteres"
                return@setOnClickListener
            }

            buscar(texto)
        }
    }
    //funcion para hacer la busqueda de los activos en base a la opcion seleccionada
    private fun buscar(texto: String) {
        lifecycleScope.launch {
            try {
                val request = ActivoRequest(Login(user, pass), "",texto)
                System.out.println("activosrequest:"+request)
                val response = apiService.getTecnicos(request)
                System.out.println("activosresponse:"+response)
                if (response.isSuccessful) {
                    val resultados = response.body()
                    System.out.println("activosresultados:"+resultados)
                    resultados?.let {activo ->
                        val okItem = activo.ResponseTecnicos?.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            val listaActivos = activo.ResponseTecnicos.filter { it.cve != null }
                            binding.recyclerResultadosTecnicos.layoutManager = LinearLayoutManager(requireContext())
                            binding.recyclerResultadosTecnicos.adapter =
                                ResultadoBuscarTecAdapter(listaActivos) { seleccionado ->
                                onItemSelected(seleccionado)
                                dismiss()
                            }
                        }

                    }

                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
