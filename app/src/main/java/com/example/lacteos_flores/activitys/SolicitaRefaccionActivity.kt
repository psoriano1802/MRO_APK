package com.example.lacteos_flores.activitys
import android.app.DatePickerDialog
import com.example.lacteos_flores.R
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.adapters.RefaccionesAdapter
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.AltaDoctosRequest
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.OrdenItem
import com.example.lacteos_flores.models.PaquetesRequest
import com.example.lacteos_flores.models.itemsDoc
import com.example.lacteos_flores.models.modelsUI.ProductoUI
import com.example.lacteos_flores.utils.BusquedaRMBottomSheet
import com.example.lacteos_flores.utils.BusquedaTecBottonSheet
import com.example.lacteos_flores.utils.Globales.showToast
import com.example.lacteos_flores.utils.Prefs
import com.example.lacteos_flores.utils.ReportePDFGenerator
import com.example.lacteos_flores.utils.ReportePDFGenerator2
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

class SolicitaRefaccionActivity : AppCompatActivity() {

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


    private lateinit var reporteGenerator: ReportePDFGenerator
    private lateinit var reportePDFGenerator2: ReportePDFGenerator2

    //parametros recibidos
    var sucursalDoc: String? = null
    var almacenDoc: String? = null//se dejara siempre el alamcen de mro = 08
    var folioDoc: String? = null
    var centroCostoDoc: String? = null
    var monedaDoc: String? = null
    var paqDoc: String? = null
    var namActivo: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicita_refacciones)

        inicializarComponentes()
        fecha()
        setupListeners()
        obetenerSucursalesWS()

        //configurarRecyclerView()
        //llenarDatosEjemplo()
    }

    private fun inicializarComponentes() {
        // Encabezado
        spinnerSucursal = findViewById(R.id.spinner_sucursal)
        spinner_almacen = findViewById(R.id.spinner_almacen)
        tvFolio = findViewById(R.id.tv_folio)
        etFecha = findViewById(R.id.et_fecha)

        // Campos principales
        tvCentroCosto = findViewById(R.id.tv_centro_costo)
        tvMoneda = findViewById(R.id.tv_moneda)
        tv_orden_mantenimiento = findViewById(R.id.tv_orden_mantenimiento)
        tv_solicita = findViewById(R.id.tv_solicita)

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
        articuloAdapter =RefaccionesAdapter(mutableListOf(),listOf("Clave","Cantida","Unidad","Preciso","Importe","Descripción"))
        rvArticulos.adapter = articuloAdapter
        rvArticulos.layoutManager = LinearLayoutManager(this)

        reporteGenerator = ReportePDFGenerator(this)
        reportePDFGenerator2 = ReportePDFGenerator2(this)

        //obtenemos los datos del usuario
        usuario = Prefs(this).obtenerUsuario().first.toString()
        pass = Prefs(this).obtenerUsuario().second.toString()

        val ordenItem = intent.getSerializableExtra("ORDEN_ITEM") as? OrdenItem
        ordenItem?.let{
            sucursalDoc = it.suc
            folioDoc = it.folio
            centroCostoDoc = it.cc
            paqDoc = it.paq
            namActivo = it.nomAct

        }
        //inicializamos con los datos recibidos
        if(ordenItem != null){
            tvCentroCosto.text = centroCostoDoc
            tvMoneda.text = "PESOS"
            tv_orden_mantenimiento.text = folioDoc
        }




    }
    //funcion para los listeners de los botones
    private fun setupListeners() {
        btnGuardar.setOnClickListener {
            // Aquí puedes implementar la lógica para guardar los datos
            //generaReporte()
            enviaSolicitud()
        }
        //boton para buscar refacciones
        btnBuscarListado.setOnClickListener {
            // Aquí puedes implementar la lógica para guardar los datos
            obtenerRefaccionesWS(paqDoc,folioDoc)// se debe enviar el paquete asignado a la orden desde lapanbtalla lista ordenes
        }
        btnAgregarRefaccion.setOnClickListener {
            // Aquí puedes implementar la lógica para guardar los datos

            mostrarDialogBusqueda()
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

        //listener para mostrar dialog de busqueda al presionarl el tvsolicita
        tv_solicita.setOnClickListener {
            dialogBuscarTecnico()
        }
        etFecha.setOnClickListener {
            showDatePicker(etFecha)
        }

    }
    //funcion para obtener sucursales y almacen validar se deja la funcion para futuras modificacines
    private fun obetenerSucursalesWS() {
        lifecycleScope.launch {
            try {

            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@SolicitaRefaccionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }


    //funcion para reallizar la busqueda de refacciones
    private fun mostrarDialogBusqueda() {
        val bottomSheet = BusquedaRMBottomSheet("1") { resultadoSeleccionado ->
            var impo = resultadoSeleccionado.costuni
            val refaccion = ProductoUI(resultadoSeleccionado.cve, resultadoSeleccionado.cant, resultadoSeleccionado.uni, resultadoSeleccionado.costuni,impo , resultadoSeleccionado.descripcion)
            System.out.println("solrefaccion:"+refaccion)
            articuloAdapter.agregarItem(refaccion)
        }
        bottomSheet.show(supportFragmentManager, "BusquedaRMBottomSheet")
    }

    //funcion para obtener las refaccion de la orden si tiene un paquete asignado
    private fun obtenerRefaccionesWS(paquete: String?, orden: String? ) {
        lifecycleScope.launch {
            try {


            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@SolicitaRefaccionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun dialogBuscarTecnico() {//ws para lista de tecnicos
        /*val bottomSheetTec = BusquedaTecBottonSheet{ tecnico ->
            var cvtec = tecnico.name
            tv_solicita.text = cvtec
        }*/
        //bottomSheetTec.show(supportFragmentManager, "BusquedaTecBottomSheet")
    }

    private fun fecha(){
        //obtenermos la fehca actual
        val fechaActual = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        etFecha.setText(fechaActual)
        tvFechaEntrega.text = fechaActual

        //obtenermos la hora y minutos actual
        val horaActual = SimpleDateFormat("HH:mm").format(System.currentTimeMillis())
        tvHoraEntrega.text = horaActual
    }
    private fun showDatePicker(campoFecha: EditText) {
        val calendario = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fechaSeleccionada = "$year-${month + 1}-$dayOfMonth"
                campoFecha.setText(fechaSeleccionada)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }
    //funcion para enviar datos al servidor para generar la solicitud de refaccion
    private fun enviaSolicitud(){
        // Aquí puedes implementar la lógica para guardar los datos
        lifecycleScope.launch {
            try {
                val login = Login(usuario.toString(), pass.toString())
                var importe = 0.0

                //tomamos los items del recyclerview para enviarlos
                val Listitems = articuloAdapter.obtenerLista()
                val lista = mutableListOf<itemsDoc>()
                for (item in Listitems){
                    //llenamos la lista de items
                    val itemDoc = itemsDoc(item.cve!!,item.cant!!.toString(),item.descripcion!!,item.uni!!,item.costuni!!.toString(),"","",item.importe!!.toString(),tv_orden_mantenimiento.text.toString(),null)
                    lista.add(itemDoc)
                    importe += item.importe!!
                }
                val request = AltaDoctosRequest(login,sucursalDoc.toString(),almacenDoc.toString(),"U","D","54","1",etFecha.text.toString(),tvMoneda.text.toString(),"1",tv_orden_mantenimiento.text.toString(),etComentarios.text.toString(),null,null,
                    null,null,null,null,null,null,null,tv_solicita.text.toString(),importe.toString(),tvFechaEntrega.text.toString(),tvHoraEntrega.text.toString(),importe.toString(),tv_orden_mantenimiento.text.toString(),tvCentroCosto.text.toString(),lista)
                System.out.println("request:"+request)
                val response = RetrofitClient.apiService.sendDoctos(request)
                System.out.println("response:"+response)
                if (response.isSuccessful) {
                    val docres = response.body()
                    System.out.println("ordenes:" + docres)
                    docres?.let { items ->
                        val okItem = items.ResponseAltaMR.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            Toast.makeText(this@SolicitaRefaccionActivity, "Solicitud enviada con exito, Folio: ${okItem.doc}", Toast.LENGTH_SHORT).show()
                            generaReporte(okItem.folio.toString(),request)
                            limpiarCampos()
                            finish()

                        }else{
                            Toast.makeText(this@SolicitaRefaccionActivity, "${okItem?.err}", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@SolicitaRefaccionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //nuevafuncion para generarel reporte simpl
    private fun generaReporte(folio: String, datos: AltaDoctosRequest){
        val partidas = mutableListOf<ReportePDFGenerator2.Partida>()
        var count = 1
        for (item in datos.items!!){

            var cve = item.kparte
            var cant = item.cant
            var uni = item.uni
            var descr =item.descri
            var importe = item.monto
            val precio = item.precio
            val partida = ReportePDFGenerator2.Partida(count.toString(),descr,cant,uni,"$precio","$importe")
            partidas.add(partida)
            count ++
        }
        System.out.println("partidas:"+partidas)
        val archivo =reportePDFGenerator2.generarArchivoConPartidas(
            sucursal = datos.suc,
            almacen = datos.alm,
            usuario = usuario.toString(),
            folio = folio,
            fecha = datos.fecha.toString(),
            activo = datos.proyecto.toString(),
            nameActivo = namActivo.toString(),
            partidas = partidas,
            comentarios = datos.comenta,
            name = "SOLICITUD DE REFACCIONES"
        )
        if (archivo != null) {
            showToast(this@SolicitaRefaccionActivity,"Archivo PDF guardado en :${archivo.absolutePath}")
        }
    }
    //funcion con distrubucion en formato
    fun generaReporte2(){
        val partidas = listOf(
            ReportePDFGenerator2.Partida("1", "Bujía NGK CR7E", "2", "pza", "$85.00", "$170.00"),
            ReportePDFGenerator2.Partida("2", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("3", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("4", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("5", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("6", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("7", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("8", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("9", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("10", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("11", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("12", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("13", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("14", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("15", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("16", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00"),
            ReportePDFGenerator2.Partida("17", "Aceite motor 10W-40", "1", "lto", "$120.00", "$120.00")
        )

        val archivo = reportePDFGenerator2.generarArchivoConPartidas(
            sucursal = "MRO",
            almacen = "08",
            usuario = "GRJ",
            folio = "25/09/2025 - 16:18",
            fecha = "25/09/2025",
            activo = "C-010",
            nameActivo = "CUATRIMOTO ITALIKA ATV 200 - ALIMENTACION",
            partidas = partidas,
            comentarios = "Sin comentarios",
            name = "SOLICITUD DE REFACCIONES"
        )
        if (archivo != null) {
            showToast(this@SolicitaRefaccionActivity,"Archivo PDF guardado en :${archivo.absolutePath}")
        }
    }
    //limpiar campos
    private fun limpiarCampos(){
        etFecha.setText("")
        tvFechaEntrega.text = ""
        tvHoraEntrega.text = ""
        etComentarios.setText("")
        tv_solicita.text = ""

    }
}