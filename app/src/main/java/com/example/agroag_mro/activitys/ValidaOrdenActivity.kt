package com.example.agroag_mro.activitys

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agroag_mro.R
import com.example.agroag_mro.adapters.DocumentosAdapter
import com.example.agroag_mro.models.modelsUI.DocumentoUI
import java.text.SimpleDateFormat
import java.util.*

class ValidaOrdenActivity : AppCompatActivity() {

    private lateinit var tvUsuario: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvActivo: TextView
    private lateinit var etDescripcionActivo: TextView
    private lateinit var rvDocumentos: RecyclerView
    private lateinit var btnValidar: Button

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
    }

    private fun configurarRecyclerView() {
        documentosAdapter = DocumentosAdapter(listaDocumentos)

        rvDocumentos.layoutManager = LinearLayoutManager(this)
        rvDocumentos.adapter = documentosAdapter
    }

    private fun cargarDatosIniciales() {
        // Usuario (puedes traerlo desde Globales)
        tvUsuario.text = "Usuario Actual"

        // Fecha actual
        val formato = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        tvFecha.text = formato.format(Date())

        // Datos simulados para prueba
        listaDocumentos.add(
            DocumentoUI("DOC001", "Correctivo", "Pendiente", "C-001", "Activo de prueba 1","P0001","Paquete 1","fallo1",true,"prueba1")
        )
        listaDocumentos.add(
            DocumentoUI("DOC002", "Preventivo", "Pendiente", "C-002", "Activo de prueba 2","P0012","Paquete3","Fallo 3",true,"prueba2")
        )

        documentosAdapter.notifyDataSetChanged()
    }

    private fun configurarEventos() {

        tvActivo.setOnClickListener {
            abrirBusquedaActivo()
        }

        btnValidar.setOnClickListener {
            validarDocumentos()
        }
    }

    private fun abrirBusquedaActivo() {
        // Aquí puedes abrir tu BottomSheet
        Toast.makeText(this, "Abrir búsqueda de activo", Toast.LENGTH_SHORT).show()
    }

    private fun validarDocumentos() {

        val documentosSeleccionados = listaDocumentos.filter { it.validar }

        if (documentosSeleccionados.isEmpty()) {
            Toast.makeText(this, "No hay documentos seleccionados", Toast.LENGTH_SHORT).show()
            return
        }

        // Aquí puedes llamar tu WS o lógica de validación
        Toast.makeText(
            this,
            "Validando ${documentosSeleccionados.size} documento(s)",
            Toast.LENGTH_SHORT
        ).show()
    }
}