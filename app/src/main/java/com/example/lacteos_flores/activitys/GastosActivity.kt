package com.example.lacteos_flores.activitys
import android.app.DatePickerDialog
import com.example.lacteos_flores.R
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.adapters.GastosAdapter
import com.example.lacteos_flores.adapters.RefaccionesAdapter
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.GastoRegistradoEntity
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.AltaDoctosRequest
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.OrdenItem
import com.example.lacteos_flores.models.modelsUI.GastosUI
import com.example.lacteos_flores.models.modelsUI.ProductoUI
import com.example.lacteos_flores.utils.BusquedaRMBottomSheet
import com.example.lacteos_flores.utils.Globales.showToast
import com.example.lacteos_flores.utils.Prefs
import com.example.lacteos_flores.utils.ReportePDFGenerator
import com.example.lacteos_flores.utils.ReportePDFGenerator2
import com.example.lacteos_flores.utils.TicketPrinter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private lateinit var db: AppDatabase
    private var catalogoGastos: List<com.example.lacteos_flores.data.GastosEntity> = listOf()

    // Launcher para permisos de Bluetooth
    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Toast.makeText(this, "Permisos concedidos. Intente de nuevo.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Se requieren permisos de Bluetooth para imprimir.", Toast.LENGTH_LONG).show()
        }
    }

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

        // Configurar Spinner desde la base de datos
        db = AppDatabase.getDatabase(this)
        cargarCatalogoGastos()

        // Recyclerview
        gastosAdapter = GastosAdapter(mutableListOf())
        rvGastos.adapter = gastosAdapter
        rvGastos.layoutManager = LinearLayoutManager(this)

        reporteGenerator = ReportePDFGenerator(this)
        reportePDFGenerator2 = ReportePDFGenerator2(this)
        db = AppDatabase.getDatabase(this)

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
            val position = spGasto.selectedItemPosition
            if (position < 0 || position >= catalogoGastos.size) {
                showToast(this, "Seleccione un tipo de gasto válido")
                return@setOnClickListener
            }
            
            val gastoSeleccionado = catalogoGastos[position]
            val tipoGastoClave = gastoSeleccionado.clave
            
            val montoStr = etMonto.text.toString()
            val comentario = etComentarios.text.toString()

            if (montoStr.isEmpty()) {
                etMonto.error = "Ingrese un monto"
                return@setOnClickListener
            }

            val monto = montoStr.toDoubleOrNull() ?: 0.0

            // Creamos un GastosUI para representar el gasto (mostramos la descripción al usuario)
            val nuevoGasto = GastosUI(
                tipoGasto = tipoGastoClave, // Guardamos la CLAVE para enviarla al WS
                monto = monto,
                comentario = comentario,
                fecha = tvFecha.text.toString()
            )

            // Si quieres mostrar la descripción en el RecyclerView, podrías necesitar ajustar el adapter 
            // o simplemente guardar la clave aquí y dejar que el adapter la muestre (o pasar la descripción)
            // Por simplicidad en el envío, guardamos la clave.
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
        // Verificar permisos antes de registrar para poder imprimir el ticket
        if (!tienePermisosBluetooth()) {
            solicitarPermisosBluetooth()
            return
        }

        val lista = gastosAdapter.obtenerLista()
        lifecycleScope.launch {
            try {
                val userEntity = db.usuarioDao().obtenerUsuario(usuario ?: "")
                
                // 1. Guardar localmente
                val gastosEntidades = lista.map { ui ->
                    GastoRegistradoEntity(
                        tipoGasto = ui.tipoGasto ?: "",
                        monto = ui.monto ?: 0.0,
                        comentario = ui.comentario ?: "",
                        fecha = ui.fecha ?: "",
                        usuario = usuario ?: "",
                        sucursal = userEntity?.cve_suc,
                        almacen = userEntity?.cve_alma,
                        sincronizado = false
                    )
                }
                
                db.gastoRegistradoDao().insertarGastos(gastosEntidades)
                showToast(this@GastosActivity, "Gastos guardados localmente")
                
                // 3. Imprimir Ticket
              //  imprimirTicketGasto(gastosEntidades)

                // 2. Intentar sincronizar en segundo plano
                // por ahora se queda a envio manual desde el sincronizador
                //sincronizarGastosKepler()
                
                // Limpiar lista y cerrar o notificar
                gastosAdapter.limpiarLista()
                finish()

            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@GastosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sincronizarGastosKepler() {
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isOnline = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!isOnline) {
            Log.w("Gastos", "Sin conexión. Sincronización pendiente.")
            return
        }

        // Usar GlobalScope para que sobreviva al finish()
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dbLocal = AppDatabase.getDatabase(applicationContext)
                val pendientes = dbLocal.gastoRegistradoDao().obtenerGastosPendientes()
                val catalogosManager = com.example.lacteos_flores.controllers.CatalogosManager(dbLocal)
                val login = Login(usuario.toString(), pass.toString())
                
                for (gasto in pendientes) {
                    try {
                        catalogosManager.enviarGasto(gasto, login)
                    } catch (e: Exception) {
                        Log.e("Gastos", "Error enviando gasto ${gasto.id}: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("Gastos", "Error en sincronización: ${e.message}")
            }
        }
    }


    //limpiar campos
    private fun limpiarCampos(){

        spGasto.setSelection(0)
        etComentarios.setText("")
        etMonto.setText("")

    }

    private fun tienePermisosBluetooth(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun solicitarPermisosBluetooth() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        requestBluetoothPermissionLauncher.launch(permissions)
    }

    private fun imprimirTicketGasto(gastos: List<GastoRegistradoEntity>) {
        val printer = TicketPrinter(this)
        printer.connectAndPrint("Printer001") {
            setAlignCenter()
            setBold(true)
            setLargeFont(false)
            printText("PRODUCTOS LACTEOS FLORES\n")

            setLargeFont(false)
            setBold(false)
            printText("R.F.C.: PLF010228TC3\n")
            printText("Calle: NICOLAS BRAVO\n")
            printText("Colonia: CENTRO\n")
            printText("Municipio: JIQUILPAN\n")
            printText("Telefono: 3535330998\n")
            printText("\n")
            printText("COMPROBANTE DE GASTO\n")
            printText("Fecha: ${tvFecha.text}\n")
            printDivider()

            setAlignLeft()
            printText("Usuario: $usuario\n")
            printDivider()

            // Formato: TIPO(10) MONTO(10)
            val headerRow = String.format(Locale.US, "%-15s %15s\n", "Tipo", "Monto")
            printText(headerRow)
            printDivider()

            var total = 0.0
            for (gasto in gastos) {
                val line = String.format(Locale.US, "%-15s %15.2f\n",
                    gasto.tipoGasto.take(15),
                    gasto.monto
                )
                printText(line)
                if (gasto.comentario.isNotEmpty()) {
                    printText("Obs: ${gasto.comentario}\n")
                }
                total += gasto.monto
            }
            printDivider()

            setAlignRight()
            setBold(true)
            printText("TOTAL GASTOS: $ ${String.format(Locale.US, "%.2f", total)}\n")
            setBold(false)

            setAlignCenter()
            printText("\nFirma del Responsable\n\n\n")
            printText("______________________\n")
            printText("\n¡Registro de Control Interno!\n")
        }
    }

    private fun cargarCatalogoGastos() {
        lifecycleScope.launch {
            try {
                catalogoGastos = db.gastosDao().obtenerTodosGastos()
                val descripciones = catalogoGastos.map { it.descripcion }
                val adapterSpinner = android.widget.ArrayAdapter(this@GastosActivity, android.R.layout.simple_spinner_item, descripciones)
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spGasto.adapter = adapterSpinner
            } catch (e: Exception) {
                Log.e("Gastos", "Error cargando catálogo: ${e.message}")
            }
        }
    }
}
