package com.example.agroag_mro.activitys

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agroag_mro.R
import com.example.agroag_mro.adapters.RefaccionesAdapter
import com.example.agroag_mro.models.AlmacenItem
import com.example.agroag_mro.models.OrdenItem
import com.example.agroag_mro.models.SucursalItem
import com.example.agroag_mro.models.SucursalResponse
import com.example.agroag_mro.models.itemsDoc
import com.example.agroag_mro.models.modelsUI.ProductoUI
import com.example.agroag_mro.utils.BusquedaRMBottomSheet
import com.example.agroag_mro.utils.BusquedaTecBottonSheet
import com.example.agroag_mro.utils.Globales
import com.example.agroag_mro.utils.Result
import kotlinx.coroutines.launch

class ManoObraActivity: AppCompatActivity() {
    // === Encabezado ===
    private lateinit var spinnerSucursal: Spinner
    private lateinit var spinner_almacen: Spinner
    private lateinit var tvFolio: TextView
    private lateinit var etFecha: EditText

    // === Campos principales ===
    private lateinit var tvCentroCosto: TextView
    private lateinit var tvMoneda: TextView
    private lateinit var tv_orden_mantenimiento: TextView
    private lateinit var tv_solicita: TextView
    private lateinit var tv_lblSolicita: TextView

    // === Lista de artículos ===
    private lateinit var rvArticulos: RecyclerView
    private lateinit var articuloAdapter: RefaccionesAdapter
    // private val listaArticulos = mutableListOf<Articulo>()

    // === Fecha y hora ===
    private lateinit var tvFechaEntrega: TextView
    private lateinit var tvHoraEntrega: TextView

    // === Comentarios ===
    private lateinit var etComentarios: EditText

    // === Botones ===
    private lateinit var btnGuardar: Button
    private lateinit var btnAgregarRefaccion: Button
    private lateinit var btnBuscarListado: Button

    //variables loclaes
    private var usuario: String? = null
    private var pass: String? = null
    private var sucursalUsurio: String? = null
    private var sucursalID: String? = null
    private lateinit var sucursales: List<SucursalItem>
    private lateinit var almacenes: List<AlmacenItem>

    //parametros recibidos
    var sucursalDoc: String? = null
    var almacenDoc: String? = null//se dejara siempre el alamcen de mro = 08
    var folioDoc: String? = null
    var centroCostoDoc: String? = null
    var monedaDoc: String? = null
    var paqDoc: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicita_refacciones)

        inicializarComponentes()

        setupListeners()
        //obetenerSucursalesWS()

    }

    //funcion para incializar componentes
    private fun inicializarComponentes(){
        // Encabezado
        spinnerSucursal = findViewById(R.id.spinner_sucursal)
        spinner_almacen = findViewById(R.id.spinner_almacen)
        tvFolio = findViewById(R.id.tv_folio)
        etFecha = findViewById(R.id.et_fecha)

        // Campos principales
        tvCentroCosto = findViewById(R.id.tv_centro_costo)
        tvMoneda = findViewById(R.id.tv_moneda)
        tv_orden_mantenimiento = findViewById(R.id.tv_orden_mantenimiento)
        tv_lblSolicita = findViewById(R.id.tv_lblSolicita)
        tv_solicita = findViewById(R.id.tv_solicita)
        tv_solicita.isVisible = false
        tv_lblSolicita.isVisible = false

        // Lista de artículos
        rvArticulos = findViewById(R.id.rv_articulos)

        // Fecha y hora
        tvFechaEntrega = findViewById(R.id.tv_fecha_entrega)
        tvHoraEntrega = findViewById(R.id.tv_hora_entrega)

        // Comentarios
        etComentarios = findViewById(R.id.et_comentarios)

        // Botones
        btnGuardar = findViewById(R.id.btn_guardar)
        btnAgregarRefaccion = findViewById(R.id.btn_agregar_refaccion)
        btnBuscarListado = findViewById(R.id.btn_buscar_listado)

        //recyclerview
        rvArticulos = findViewById(R.id.rv_articulos)
        articuloAdapter =RefaccionesAdapter(mutableListOf(), listOf("Clave","Min","Hr","Unidad","Técnico"))
        rvArticulos.adapter = articuloAdapter
        rvArticulos.layoutManager = LinearLayoutManager(this)



        val ordenItem = intent.getSerializableExtra("ORDEN_ITEM") as? OrdenItem
        ordenItem?.let{
            sucursalDoc = it.suc
            folioDoc = it.folio
            centroCostoDoc = it.cc
            paqDoc = it.paq

        }
        //inicializamos con los datos recibidos
        if(ordenItem != null){
            tvCentroCosto.text = centroCostoDoc
            tvMoneda.text = "PESOS"
            tv_orden_mantenimiento.text = folioDoc
        }
        fecha()
        obtenerSuc()

    }
    //funcion para configurar listeners
    private fun setupListeners() {
        btnBuscarListado.setOnClickListener {
            obtenerRefaccionesWS(paqDoc,folioDoc)
        }
        btnAgregarRefaccion.setOnClickListener {
            mostrarDialogBusqueda()

        }
        btnGuardar.setOnClickListener {
            enviaSolicitud()
        }
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                articuloAdapter.eliminarItem(position)
            }
        })
        itemTouchHelper.attachToRecyclerView(rvArticulos)

    }
    //funcuin para obtener fecha y hora
    private fun fecha(){
        //obtenermos la fehca actual
        val fechaActual = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        etFecha.setText(fechaActual)
        tvFechaEntrega.text = fechaActual

        //obtenermos la hora y minutos actual
        val horaActual = SimpleDateFormat("HH:mm").format(System.currentTimeMillis())
        tvHoraEntrega.text = horaActual
    }
    private fun obtenerSuc(){
        lifecycleScope.launch {
            when (val result = Globales.obtenerSucursales()){
                is Result.Success -> {
                   val suc = result.data
                    setupSpinnerSucursal(suc,sucursalDoc)
                }
                is Result.Empty ->{
                    Globales.showToast(this@ManoObraActivity, "No se encontraron sucursales")
                }
                is Result.Error ->{
                    Globales.showToast(this@ManoObraActivity, "Error al obtener sucursales: ${result.exception.message}")
                }
            }


        }
    }

    private fun setupSpinnerSucursal(response: SucursalResponse, sucDoc: String? = null) {
        //llenamos el spinner de la sucursal y almacen
        sucursales = response.ResponseSucursal.filter { it.cve != null }
        val spSucursales = sucursales.map { it.suc }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spSucursales)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSucursal.adapter = adapter

        //la sucursal de dejara fija tomando la que el documento seleccionado tenga asignada
        sucDoc?.let { guardada ->
            val index = sucursales.indexOfFirst { it.cve == guardada }
            if (index >= 0) {
                spinnerSucursal.isEnabled = false
                spinnerSucursal.setSelection(index)
                sucursalID = sucursales[index].cve.toString()
                // ✅ También cargamos almacenes de esa sucursal directamente
                cargarSpinnerAlmacen(sucursales[index])
            }
        }

        spinnerSucursal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccionada = sucursales[position]
                System.out.println("sucursal seleccionada:"+seleccionada.cve)
                sucursalID = seleccionada.cve.toString()
                cargarSpinnerAlmacen(seleccionada)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ✅ Función auxiliar para cargar almacenes y fijar el 08
    private fun cargarSpinnerAlmacen(sucursal: SucursalItem) {
        almacenes = sucursal.alma ?: emptyList()
        val spAlmacenes = almacenes.map { it.alm } // asumo que `alma` es el nombre o clave
        val adapterAlm = ArrayAdapter(this, android.R.layout.simple_spinner_item, spAlmacenes)
        adapterAlm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_almacen.adapter = adapterAlm

        // ✅ Buscar el índice del almacén "08" y fijarlo
        val index08 = almacenes.indexOfFirst { it.cvea == "8" }
        if (index08 >= 0) {
            spinner_almacen.setSelection(index08)
            spinner_almacen.isEnabled = false // lo bloqueamos si quieres fijo
            almacenDoc = almacenes[index08].cvea.toString()
        }

        spinner_almacen.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val almacenSel = almacenes[position]
                almacenDoc = almacenSel.cvea.toString()
                println("Almacén seleccionado: $almacenDoc")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    //funcion para reallizar la busqueda de refacciones
    private fun mostrarDialogBusqueda() {
        val bottomSheet = BusquedaRMBottomSheet("2") { res ->
            var impo = res.costuni
            val hr = res.cant
            val min = hr?.times(60.0)
            val refaccion = ProductoUI(res.cve, 0.0, res.uni,0.0,min,res.descripcion , min,hr)

            System.out.println("refaccionMO:"+res)
            dialogBuscarTecnico(refaccion)
            //articuloAdapter.agregarItem(refaccion)
        }
        bottomSheet.show(supportFragmentManager, "BusquedaRMBottomSheet")
    }
    private fun dialogBuscarTecnico(refaccion: ProductoUI) {//ws para lista de tecnicos
        val bottomSheetTec = BusquedaTecBottonSheet{ tecnico ->
            val reftecnico= refaccion.copy(descripcion = tecnico.cve)
            System.out.println("reftecnico:"+reftecnico)
            articuloAdapter.agregarItem(reftecnico)
        }
        bottomSheetTec.show(supportFragmentManager, "BusquedaTecBottomSheet")
    }

    private fun obtenerRefaccionesWS(paquete: String?, orden: String? ) {
        lifecycleScope.launch {
            when (val result = Globales.obtenesRM(orden,paquete,"2")) {
                is Result.Success ->{
                    val paqRes = result.data
                    println("refacciones:"+paqRes)
                    paqRes?.let { mo ->
                        val okItem = mo.ResponsePaquetes.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            val opera = mo.ResponsePaquetes.filter { it.cver != null }
                            if(opera.isNotEmpty()){
                                val listMO = mutableListOf<ProductoUI>()
                                for(item in opera){
                                    val hr = item.tespe!!.toDouble()
                                    val min =hr * 60
                                    val prod = ProductoUI(item.cver,0.0,item.uni,0.0,hr,item.namer,min,hr)
                                    listMO.add(prod)
                                }
                                articuloAdapter.agregarLista(listMO)
                            }else{
                                Globales.showToast(this@ManoObraActivity, "No se encontraron refacciones")
                            }
                        }else{
                            Globales.showToast(this@ManoObraActivity, "No se encontraron refacciones")
                        }

                    }
                }

                is Result.Empty ->{
                    Globales.showToast(this@ManoObraActivity, "No se encontraron refacciones")
                }
                is Result.Error ->{
                    Globales.showToast(this@ManoObraActivity, "Error al obtener refacciones: ${result.exception.message}")

                }
            }

        }
    }
    private fun enviaSolicitud(){
        // Aquí puedes implementar la lógica para guardar los datos
        lifecycleScope.launch {

                var importe = 0.0
                val fec = etFecha.text.toString()
                val mon = tvMoneda.text.toString()
                val orde = tv_orden_mantenimiento.text.toString()
                val comen = etComentarios.text.toString()
                val fe = tvFechaEntrega.text.toString()
                val te = tvHoraEntrega.text.toString()
                val depto = tvCentroCosto.text.toString()

                //tomamos los items del recyclerview para enviarlos
                val Listitems = articuloAdapter.obtenerLista()
                val lista = mutableListOf<itemsDoc>()
                for (item in Listitems){
                    //llenamos la lista de items
                    val itemDoc = itemsDoc(item.cve!!,item.cant!!.toString(),item.importe!!.toString(),item.uni!!,item.costuni!!.toString(),tv_orden_mantenimiento.text.toString(),item.descripcion!!,"",null)
                    println("itemdoc:"+itemDoc)
                    lista.add(itemDoc)
                    importe += item.cant!!
                }

                when(val result = Globales.sendDocto(sucursalID.toString(),almacenDoc.toString(),"U","D","52","2",fec,mon,"1",orde,comen,"",importe.toString(),fe,te,importe.toString(),orde,depto,lista)){
                    is Result.Success ->{
                        val alta = result.data
                        println("alta:"+alta)
                        alta?.let { doc ->
                            val okItem = doc.ResponseAltaMR.find { it.ok != null }
                            if (okItem?.ok == "1") {
                                Globales.showToast(this@ManoObraActivity, "Documento generado correctamente: "+okItem.doc)
                                limpia_Campos()
                                finish()
                            } else {
                                Globales.showToast(this@ManoObraActivity, "Error al generar el documento")
                            }


                        }
                    }
                    is Result.Empty ->{
                        Globales.showToast(this@ManoObraActivity, "No se pudo enviar la solicitud")
                    }
                    is Result.Error -> {
                        Globales.showToast(this@ManoObraActivity,"Error al enviar la solicitud: ${result.exception.message}")
                    }

                }

        }

    }
    //limpiar campos
    private fun limpia_Campos(){
        tvCentroCosto.text = ""
        tvMoneda.text = ""
        tv_orden_mantenimiento.text = ""
        tv_solicita.text = ""
        etComentarios.text.clear()
        //actualiza fecha y hora}
        fecha()
        //limpiar lista
    }
}