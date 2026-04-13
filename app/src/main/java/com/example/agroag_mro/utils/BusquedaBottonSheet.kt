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
import com.example.agroag_mro.databinding.DialogBusquedaBinding
import com.example.agroag_mro.interfaz.ApiService
import com.example.agroag_mro.interfaz.RetrofitClient
import com.example.agroag_mro.models.ActivoRequest
import com.example.agroag_mro.models.Activos
import com.example.agroag_mro.models.ActivosResponse
import com.example.agroag_mro.models.Login
import com.example.agroag_mro.models.LoginRequest
import com.example.agroag_mro.models.TiposActivoResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class BusquedaBottomSheet(
    private val tipobusqueda: String,
    private val onItemSelected: (Activos) -> Unit // Callback al seleccionar un resultado
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
            if(tipobusqueda.equals("1")){
                buscar(opcionSeleccionada, texto)
            }else{
                buscarAU(opcionSeleccionada, texto)
            }
        }
    }

    private fun cargarOpciones() {

        lifecycleScope.launch {
            try {
                val request = LoginRequest(Login(user , pass))
                System.out.println("tiposrequest:"+request)
                val response = apiService.getTipoActivo(request)
                System.out.println("tiposresponse:"+response)
                if (response.isSuccessful) {
                    val opciones = response.body()
                    System.out.println("tiposopciones:"+opciones)
                    opciones?.let { tiposActivo ->
                        val okItem = tiposActivo?.ResponseTiposActivos?.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            setupTiposActivo(tiposActivo)
                        }

                    }

                }
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

                val request = ActivoRequest(Login(user, pass), opcion, texto)
                System.out.println("activosrequest:"+request)
                val response = apiService.getActivos(request)
                System.out.println("activosresponse:"+response)
                if (response.isSuccessful) {
                    val resultados = response.body()
                    System.out.println("activosresultados:"+resultados)
                    resultados?.let {activo ->
                        val okItem = activo.ResponseActivos?.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            val listaActivos = activo.ResponseActivos.filter { it.cve != null }
                            binding.recyclerResultados.layoutManager = LinearLayoutManager(requireContext())
                            binding.recyclerResultados.adapter = ResultadoAdapter(listaActivos) { seleccionado ->
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
    private fun buscarAU(opcion: String, texto: String) {
        lifecycleScope.launch {
            try {

                val request = ActivoRequest(Login(user, pass), opcion, texto)
                System.out.println("activosrequestUA:"+request)
                val response = apiService.getActivoUsr(request)
                System.out.println("activosresponseUA1:"+response)
                //imprimimos el json contruido
                println("activosresponseUA:"+response.body())
                if (response.isSuccessful) {
                    val resultados = response.body()
                    println("activosresultadosUARes:"+resultados)
                    resultados?.let {activo ->
                        val okItem = activo.ResponseActivos?.find { it.ok != null }
                        System.out.println("activosresultadosokua:"+okItem)
                        if (okItem?.ok == "1") {
                            val listaActivos = activo.ResponseActivos.filter { it.cve != null }
                            binding.recyclerResultados.layoutManager = LinearLayoutManager(requireContext())
                            binding.recyclerResultados.adapter = ResultadoAdapter(listaActivos) { seleccionado ->
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
