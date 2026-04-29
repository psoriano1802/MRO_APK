package com.example.agroag_mro.activitys

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agroag_mro.R
import com.example.agroag_mro.adapters.DocumentosAdapter
import com.example.agroag_mro.adapters.ResultadoAdapter
import com.example.agroag_mro.interfaz.RetrofitClient.apiService
import com.example.agroag_mro.models.Login
import com.example.agroag_mro.models.OrdenItem
import com.example.agroag_mro.models.OrdenesRequestActivo
import com.example.agroag_mro.models.OrdenesRequestUsuario
import com.example.agroag_mro.models.SendOrdenes
import com.example.agroag_mro.models.modelsUI.DocumentoUI
import com.example.agroag_mro.utils.BusquedaBottomSheet
import com.example.agroag_mro.utils.Prefs
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ValidaOrdenActivity : AppCompatActivity() {

    private lateinit var tvUsuario: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvActivo: TextView
    private lateinit var etDescripcionActivo: TextView
    private lateinit var rvDocumentos: RecyclerView
    private lateinit var btnValidar: Button
    private lateinit var btnBuscarActivo: Button
    //variables para obtener el usuario y la contraseña de las preferencias
    private lateinit var user: String
    private lateinit var pass: String


    private lateinit var documentosAdapter: DocumentosAdapter
    private val listaDocumentos = mutableListOf<DocumentoUI>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_valida_orden)

        inicializarVistas()
        configurarRecyclerView()
        cargarDatosIniciales()
        configurarEventos()
    }

    private fun inicializarVistas() {
        tvUsuario = findViewById(R.id.tv_usuario)
        tvFecha = findViewById(R.id.tv_fecha)
        tvActivo = findViewById(R.id.tv_activo)
        etDescripcionActivo = findViewById(R.id.tv_descripcion_activo)
        rvDocumentos = findViewById(R.id.rv_documentos)
        btnValidar = findViewById(R.id.btn_validar)
        btnBuscarActivo = findViewById(R.id.btnBusOrdenAct)
    }

    private fun configurarRecyclerView() {
        documentosAdapter = DocumentosAdapter(listaDocumentos)

        rvDocumentos.layoutManager = LinearLayoutManager(this)
        rvDocumentos.adapter = documentosAdapter

        //obtenermos los datos de las preferencias
        val prefs = Prefs(this)
        user = prefs.obtenerUsuario().first.toString()
        pass = prefs.obtenerUsuario().second.toString()
    }

    private fun cargarDatosIniciales() {
        // Usuario (puedes traerlo desde Globales)
        tvUsuario.text =user.toString()

        // Fecha actual
        val formato = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        tvFecha.text = formato.format(Date())

    }

    private fun configurarEventos() {

        tvActivo.setOnClickListener {
            mostrarDialogoBusquedaActivos()
        }

        btnValidar.setOnClickListener {
            validarDocumentos()
        }
        btnBuscarActivo.setOnClickListener {
            buscaOrdenes()
        }
    }

    private fun buscaOrdenes() {
        // Aquí puedes abrir tu BottomSheet
        lifecycleScope.launch {
            try {
                val request = OrdenesRequestActivo(Login(user.toString(), pass.toString()), tvActivo.text.toString())
                System.out.println("orderxusr:"+request)
                val response = apiService.getOrdenesActivo(request)
                System.out.println("tiposresponse:"+response)
                if (response.isSuccessful) {
                    val opciones = response.body()
                    System.out.println("tiposopciones:"+opciones)
                    opciones?.let { tiposActivo ->
                        val okItem = tiposActivo?.ResponseBusOrd?.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            listaDocumentos.clear()
                            val listaOrdenes = tiposActivo.ResponseBusOrd.filter { it.orden != null }
                            for (orden in listaOrdenes) {
                                //ord.add(OrdenItem(null,orden.folio!!,null, orden.nomAct!!,null, orden.tipo!!,null,null,null,null,null,null,null,null,null))
                                listaDocumentos.add(DocumentoUI(orden.orden!!,orden.tipo!!, orden.abc!!,orden.activo!!,orden.nactivo!!, orden.paque!!,orden.npaque!!,orden.obser!!,false,""))
                                println("listaDocumentos:"+orden.orden)
                            }
                            // listaOrdenes.addAll(ord)
                            System.out.println("listaOrdenes:"+listaOrdenes)
                            documentosAdapter.notifyDataSetChanged()

                        }
                    }
                }
            } catch (e: Exception) {
               println("Error al cargar opciones,e:"+e.message)
            }
        }
        Toast.makeText(this, "Abrir búsqueda de activo", Toast.LENGTH_SHORT).show()
    }
    //funcion para mostrar el dialogo de busqueda de activos
    private fun mostrarDialogoBusquedaActivos   () {
        val bottomSheet = BusquedaBottomSheet("2") { resultadoSeleccionado ->
            tvActivo.text = resultadoSeleccionado.cve
            etDescripcionActivo.text = resultadoSeleccionado.name
        }
        bottomSheet.show(supportFragmentManager, "BusquedaBottomSheet")


    }

    private fun validarDocumentos() {

        //filtramos los documentos que tengan check valido y un comentario agregado en el recyclerview

        val documentosSeleccionados = documentosAdapter.getDocumentosSeleccionadosConComentario()


        if (documentosSeleccionados.isEmpty()) {
            Toast.makeText(this, "No hay documentos seleccionados", Toast.LENGTH_SHORT).show()
            return
        }
        //mostramos un dialogo de confirmacion
        AlertDialog.Builder(this)
            .setTitle("Confirmación")
            .setMessage("Confirmación, valide que las órdenes estén seleccionadas y que contengan sus comentarios")
            .setPositiveButton("Aceptar") { dialog, _ ->
                // Este botón solo quitará el mensaje y seguirá con el proceso
                dialog.dismiss()

                //funcion que enviara los datos por el ws a  kepler y se limpian los datos
                lifecycleScope.launch {
                    try {
                        //enviamos los datos seleccionados
                        //convertimos la lista de documentos seleccionados a una lista de sendordenes
                        val sendOrdenes = documentosSeleccionados.map {
                            SendOrdenes(it.documento, it.comentario)
                        }
                        val request = OrdenesRequestUsuario(Login(user.toString(), pass.toString()), sendOrdenes)
                        //convertimos el request a json para imprimirlo en consola
                        val jsonEnviado = com.google.gson.Gson().toJson(request)
                        println("DEBUG JSON ENVIADO: $jsonEnviado")
                        System.out.println("orderxusr:"+request)
                        val response = apiService.sendValidaOrdenes(request)
                        //mostramos el resultado de la validacion
                        if (response.isSuccessful) {
                            val opciones = response.body()
                            System.out.println("tiposopciones:"+opciones)
                            opciones?.let { tiposActivo ->
                                val okItem =
                                    tiposActivo?.ResponseValidaOrdenes?.find { it.ok != null }

                                if (okItem?.ok == "1") {
                                    Toast.makeText(
                                        this@ValidaOrdenActivity,
                                        "Ordenes validadas correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }else{
                                    Toast.makeText(
                                        this@ValidaOrdenActivity,
                                        "Error al validar ordenes",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }else{
                            println("Error al cargar opciones,e:"+response.message())
                        }


                    }catch (e: Exception){
                        println("Error al enviar datos,e:"+e.message)
                    }
                }

            }
            .show()


    }
}