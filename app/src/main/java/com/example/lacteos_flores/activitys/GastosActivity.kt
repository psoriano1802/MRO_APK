package com.example.lacteos_flores.activitys
import android.app.DatePickerDialog
import com.example.lacteos_flores.R
import android.os.Bundle
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
import com.example.lacteos_flores.adapters.GastosAdapter
import com.example.lacteos_flores.adapters.RefaccionesAdapter
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.AltaDoctosRequest
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.OrdenItem
import com.example.lacteos_flores.models.itemsDoc
import com.example.lacteos_flores.models.modelsUI.GastosUI
import com.example.lacteos_flores.models.modelsUI.ProductoUI
import com.example.lacteos_flores.utils.BusquedaRMBottomSheet
import com.example.lacteos_flores.utils.Globales.showToast
import com.example.lacteos_flores.utils.Prefs
import com.example.lacteos_flores.utils.ReportePDFGenerator
import com.example.lacteos_flores.utils.ReportePDFGenerator2
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

class GastosActivity : AppCompatActivity() {



    // === Campos principales ===
    private lateinit var spGasto: Spinner
    private lateinit var etMonto: EditText
    // === Comentarios ===
    private lateinit var etComentarios: EditText

    // === Botones ===
    private lateinit var btnGuardar: Button
    private lateinit var btnAddGasto: Button
    private lateinit var rvGastos: RecyclerView

    //variables loclaes
    private var usuario: String? = null
    private var pass: String? = null
    private var sucursalUsurio: String? = null
    private var sucursalID: String? = null

    private lateinit var tvFecha: TextView

    private lateinit var reporteGenerator: ReportePDFGenerator
    private lateinit var reportePDFGenerator2: ReportePDFGenerator2
    private lateinit var gastosAdapter: GastosAdapter

    //parametros recibidos
    var sucursalDoc: String? = null
    var almacenDoc: String? = null//se dejara siempre el alamcen de mro = 08
    var folioDoc: String? = null
    var centroCostoDoc: String? = null
    var paqDoc: String? = null
    var namActivo: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicita_refacciones)

        inicializarComponentes()
        fecha()
        setupListeners()


        //configurarRecyclerView()
        //llenarDatosEjemplo()
    }

    private fun inicializarComponentes() {
        // Encabezado
        spGasto = findViewById(R.id.spinner_tipo_gasto)
        etMonto = findViewById(R.id.et_monto_gasto)
        etComentarios = findViewById(R.id.et_comentario_gasto)
        tvFecha = findViewById(R.id.tv_fecha_gasto)

        // Lista de artículos
        rvGastos = findViewById(R.id.rv_gastos)

        // Botones
        btnGuardar = findViewById(R.id.btn_guardar)
        btnAddGasto = findViewById(R.id.btn_agregar_gasto)

        // Configurar Spinner con datos de ejemplo
        val opcionesGastos = listOf("Combustible", "Mantenimiento", "Viáticos", "Otros")
        val adapterSpinner = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesGastos)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGasto.adapter = adapterSpinner

        // Recyclerview
        gastosAdapter = GastosAdapter(mutableListOf())
        rvGastos.adapter = gastosAdapter
        rvGastos.layoutManager = LinearLayoutManager(this)

        reporteGenerator = ReportePDFGenerator(this)
        reportePDFGenerator2 = ReportePDFGenerator2(this)

        // Obtenemos los datos del usuario
        usuario = Prefs(this).obtenerUsuario().first.toString()
        pass = Prefs(this).obtenerUsuario().second.toString()
    }
    //funcion para los listeners de los botones
    private fun setupListeners() {
        btnGuardar.setOnClickListener {
            if (gastosAdapter.obtenerLista().isEmpty()) {
                showToast(this, "Agregue al menos un gasto")
                return@setOnClickListener
            }
            enviaSolicitud()
        }

        //boton para agregar gastos a la lista
        btnAddGasto.setOnClickListener {
            val tipoGasto = spGasto.selectedItem.toString()
            val montoStr = etMonto.text.toString()
            val comentario = etComentarios.text.toString()

            if (montoStr.isEmpty()) {
                etMonto.error = "Ingrese un monto"
                return@setOnClickListener
            }

            val monto = montoStr.toDoubleOrNull() ?: 0.0

            // Creamos un GastosUI para representar el gasto
            val nuevoGasto = GastosUI(
                tipoGasto = tipoGasto,
                monto = monto,
                comentario = comentario,
                fecha = tvFecha.text.toString()
            )

            gastosAdapter.agregarGasto(nuevoGasto)
            limpiarCampos()
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
                gastosAdapter.eliminarGasto(position)
            }
        })
        itemTouchHelper.attachToRecyclerView(rvGastos)
    }
    //funcion para obtener sucursales y almacen validar se deja la funcion para futuras modificacines

    private fun fecha(){
        //obtenermos la fehca actual
        val fechaActual = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        tvFecha.text = fechaActual

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

            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@GastosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
            showToast(this@GastosActivity,"Archivo PDF guardado en :${archivo.absolutePath}")
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
            showToast(this@GastosActivity,"Archivo PDF guardado en :${archivo.absolutePath}")
        }
    }
    //limpiar campos
    private fun limpiarCampos(){

        spGasto.setSelection(0)
        etComentarios.setText("")
        etMonto.setText("")

    }
}