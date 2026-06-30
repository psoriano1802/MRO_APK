package com.example.lacteos_flores.activitys

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.lacteos_flores.databinding.ActivityJornadaBinding
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.UsuarioDao
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.ubicacionRequest
import com.example.lacteos_flores.models.Validadia
import com.example.lacteos_flores.utils.Prefs
import com.example.lacteos_flores.R
import com.example.lacteos_flores.controllers.CatalogosManager
import com.example.lacteos_flores.models.LoginRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class JornadaActivity: AppCompatActivity() {
    private lateinit var binding: ActivityJornadaBinding
    private lateinit var db: AppDatabase
    private lateinit var loginUserDao: UsuarioDao
    // Referencias a los views
    private lateinit var tvUser: TextView
    private lateinit var tvFecha: TextView
    private lateinit var btnIniDia: Button
    private lateinit var btnFinDia: Button
    private lateinit var btnMenu: Button

    private lateinit var gpsHelper: GpsHelper
    //vairables locales
    private var usuario: String? = null
    private var pass: String? = null

    // Instanciamos el manager (Asegúrate de pasar tu instancia real de Room Database)
    private lateinit var catalogosManager: CatalogosManager

    // 1. Lanzador de permisos en Kotlin
    // Lanzador para solicitar permisos de GPS
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            ejecutarEnvioDeUbicacion(TipoJornada.INICIO)
        } else {
            Toast.makeText(this, "Se requiere el GPS para registrar la actividad", Toast.LENGTH_LONG).show()
        }
    }
    //para manejo de inicio y fin de jornada
    enum class TipoJornada { INICIO, FIN }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jornada)
        usuario = Prefs(this).obtenerUsuario().first.toString()
        pass = Prefs(this).obtenerUsuario().second.toString()
        gpsHelper = GpsHelper(this)
        //inicializamos la base de datos para obtenes informacion del usaurio
        db = AppDatabase.getDatabase(this)
        loginUserDao = db.usuarioDao()
        // Inicializa el manager con tu base de datos
        // val db = Room.databaseBuilder(...).build()
        catalogosManager = CatalogosManager(db)
        obtenerUser()
        // Inicializar views
       initViews()


        // obtenemmos fecha actual
        fecha()
        // Configurar listeners de botones
        setupButtonListeners()

        // Validar el estado de la jornada al iniciar
        validarEstadoJornada()
    }

    private fun validarEstadoJornada() {
        lifecycleScope.launch {
            try {
                val login = Login(usuario.toString(), pass.toString())
                val response = RetrofitClient.apiService.validaDia(LoginRequest(login))
                if (response.isSuccessful) {
                    val validaDia = response.body()?.ValidaDiaResponse?.getOrNull(0)
                    if (validaDia != null) {
                        procesarValidacion(validaDia)
                    }
                }
            } catch (e: Exception) {
                showToast("Error al validar estado: ${e.message}")
            }
        }
    }

    private fun procesarValidacion(valida: Validadia) {
        val fechaInicioStr = valida.fecini // Formato: "2026-06-04 12:37:31"
        val fechaTerminaStr = valida.fecter // Formato: "-" o fecha

        val sdf = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val fechaHoy = sdf.format(System.currentTimeMillis())


        if (fechaInicioStr == null || fechaInicioStr == "-") {
            // 3. Si no hay un inicio de jornada habilitar el Iniciar Jornada
            actualizarInterfaz(iniciar = true, terminar = false)
            return
        }

        if (fechaTerminaStr == null || fechaTerminaStr == "-") {
            // 1. Si existe existe un inicio de jornada pero no hay una terminacion se habilitara Terminar Jornada.
            actualizarInterfaz(iniciar = false, terminar = true)
        } else {
            // 2. Si ya hay un inicio y termina jornada se inhabilitan ambas (si fue hoy)
            val soloFechaTermina = fechaTerminaStr.split(" ")[0]
            if (soloFechaTermina == fechaHoy) {
                actualizarInterfaz(iniciar = false, terminar = false)
            } else {
                // Si la terminación fue de otro día, hoy no tiene inicio todavía
                actualizarInterfaz(iniciar = true, terminar = false)
            }
        }
    }

    //funcion para inicializar los views
    private fun initViews() {
        //textviews

        tvFecha = findViewById(R.id.tv_fecha)

        //botones
        btnIniDia = findViewById(R.id.btn_ini_dia)
        btnFinDia = findViewById(R.id.btn_fin_dia)
        btnMenu = findViewById(R.id.btn_menu)
        tvUser = findViewById(R.id.tv_usuario)



    }

    //funcion para obtener la fecha actual
    private fun fecha(){
        val fechaActual = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        tvFecha.text = fechaActual
    }
   //funcion para configurar los listeners de los botones
    private fun setupButtonListeners() {
        btnIniDia.setOnClickListener {
            //permitira iniciar el dia de labores
            verificarPermisosYEjecutar(TipoJornada.INICIO)
        }
        btnFinDia.setOnClickListener {
            //permitira terminar el dia de labores
            validarFinJornada()
        }
        btnMenu.setOnClickListener {
            //regresa al menu principal
            finish()
        }
    }

    private fun validarFinJornada() {
        lifecycleScope.launch {
            // 1. Validar si hay existencias pero no se ha registrado una descarga
            val hayStock = db.existenciasDao().obtenerStockAgrupadoTMC().isNotEmpty()
            val hayDescarga = db.kdm1Dao().obtenerMovimientos().any { it.grp == "25" && it.tip=="18"}

            if (hayStock && !hayDescarga) {
                mostrarDialogoDescargaPendiente()
                return@launch
            }

            // 2. Validar si hay documentos pendientes por sincronizar
            val pendientesMov = db.kdm1Dao().obtenerMovimientos().any { it.staSinc == "N" }
            val pendientesGastos = db.gastoRegistradoDao().obtenerGastosPendientes().isNotEmpty()

            if (pendientesMov || pendientesGastos) {
                mostrarDialogoSincronizacion()
            } else {
                verificarPermisosYEjecutar(TipoJornada.FIN)
            }
        }
    }

    private fun mostrarDialogoDescargaPendiente() {
        AlertDialog.Builder(this)
            .setTitle("Existencias Pendientes")
            .setMessage("Aún cuenta con existencias en su inventario local. Por favor, realice la 'Descarga' de sus productos antes de finalizar la jornada.")
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarDialogoSincronizacion() {
        AlertDialog.Builder(this)
            .setTitle("Documentos Pendientes")
            .setMessage("Hay documentos pendientes por sincronizar. Se procederá a sincronizarlos antes de finalizar la jornada.")
            .setPositiveButton("Aceptar", null)
            /*.setPositiveButton("Aceptar") { _, _ ->
                procederSincronizacionYFin()
            }*/
            //.setNegativeButton("Cancelar", null)
            .show()
    }

    private fun procederSincronizacionYFin() {
        lifecycleScope.launch {
            showToast("Sincronizando movimientos pendientes...")
            val login = Login(usuario.toString(), pass.toString())
            val exito = catalogosManager.enviarTodoYLimpiar(login)
            if (exito) {
                showToast("Sincronización exitosa")
                verificarPermisosYEjecutar(TipoJornada.FIN)
            } else {
                showToast("Error al sincronizar algunos documentos. Verifique su conexión.")
                // Opcional: ¿Permitir finalizar de todos modos o no? 
                // El requerimiento dice "no permita finalizar la jornada"
            }
        }
    }

    //funcion para enviar datos para iniciar dia de labores
    private fun verificarPermisosYEjecutar(tipo: TipoJornada) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ejecutarEnvioDeUbicacion(tipo)
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }


    private fun ejecutarEnvioDeUbicacion(tipo: TipoJornada) {
        // Usamos lifecycleScope para consultar la base de datos en un hilo de fondo
        lifecycleScope.launch {
            // 1. Obtenemos el objeto completo del usuario desde la DB
            val datosUsuario = loginUserDao.obtenerUsuario(usuario.toString())
            val almacenUsuario = datosUsuario?.almacen ?: ""

            // 2. Procedemos con la ubicación
            gpsHelper.obtenerUbicacionActual(
                onSuccess = { lat, lon, address ->
                    // Cálculo de fecha para el cierre según requerimiento
                    var fechaEnvio: String? = null
                    if (tipo == TipoJornada.FIN) {
                        val now = java.util.Calendar.getInstance()
                        val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
                        val minute = now.get(java.util.Calendar.MINUTE)

                        // Rango: 11:59 AM (11:59) a 7:00 AM (07:00)
                        val enRangoAnterior = (hour > 11 || (hour == 11 && minute >= 59)) || (hour < 7)

                        if (enRangoAnterior) {
                            // Cerrar con fecha del día anterior a las 11:59 PM (23:59)
                            val cal = java.util.Calendar.getInstance()
                            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                            val sdfFecha = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            fechaEnvio = "${sdfFecha.format(cal.time)} 23:59:00"
                        } else {
                            // Cerrar con fecha del día actual
                            fechaEnvio = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(now.time)
                        }
                    }

                    // 3. Construimos el request con los datos de la DB y el GPS
                    val requestData = ubicacionRequest(
                        login = Login(user = usuario.toString(), pass = pass.toString()),
                        li = lat.toString(),
                        lo = lon.toString(),
                        dire = address,
                        nom = almacenUsuario,
                        lf = lat.toString(),
                        lof = lon.toString(),
                        diref = address,
                        fecha = fechaEnvio
                    )

                    println("requestData completo: $requestData")

                    // 4. Enviamos al servidor
                    enviarDatosAlServidor(requestData, tipo)
                },
                onError = { mensajeError ->
                    showToast(mensajeError)
                }
            )
        }
    }
    //inici la jornada
    private fun enviarDatosAlServidor(request: ubicacionRequest, tipo: TipoJornada) {
        ///para visualizar el json enviado al ws
        val jsonEnviado = com.google.gson.Gson().toJson(request)
        println("DEBUG JSON ENVIADO: $jsonEnviado")

        lifecycleScope.launch {
            try {
                if(tipo == TipoJornada.INICIO){
                    val response = RetrofitClient.apiService.sendIniDia(request)
                    if (response.isSuccessful){
                        val res = response.body()
                        val item = res?.IniciaDiaResponse?.getOrNull(0)
                        if (item?.ok.equals("1")){
                            showToast( item?.msn ?: "Jornada Iniciada")
                            Prefs(this@JornadaActivity).setJornadaActiva(true)
                            actualizarInterfaz(iniciar = false, terminar = true)
                            showToast("Comenzando la sincronizacion de catalogos...")
                            sincronizarCatalogos()
                        }else{
                            showToast(item?.msn ?: "Error al iniciar la jornada")
                        }
                    }
                }else{
                    // Manejo para FIN
                    val response = RetrofitClient.apiService.sendTerDia(request)
                    if (response.isSuccessful) {
                        val body = response.body()
                        val item = body?.TerminaDiaResponse?.getOrNull(0)
                        if (item?.ok == "1") {
                            showToast(item.msn ?: "Jornada Terminada")
                            // Después de terminar, validamos de nuevo para bloquear ambos botones si es necesario
                            validarEstadoJornada()
                        } else {
                            showToast("Error: ${item?.msn}")
                        }
                    }
                }
            }catch (e: Exception){
                showToast("Error: ${e.message}")
            }
        }
    }

    //funcion para sincronizar catalgos iniciales y guardar en la base de datos
    //catalogos clientes, productos (ctrl auxilizares y existencias), estados de cuenta(por validar), documentos,
    // descargar si las hay, paridades, moneda, bacnos
    private fun sincronizarCatalogos() {
        val login = Login(usuario.toString(), pass.toString()) // Usa tus variables reales

        lifecycleScope.launch {
            //progressbar para indicar la sincronizacion de catalogos
            var listaProds = loginUserDao.obtenerUsuario(usuario.toString())
            catalogosManager.sincronizarTodos(
                login = login,
                lista = listOf(listaProds?.lista.toString()),
                onProgress = { mensajeProgreso ->
                    // Esto se ejecuta cada vez que termina un catálogo
                    // Ideal para actualizar un TextView de estado (ej. "Descargando artículos...")
                    showToast(mensajeProgreso)
                },
                onResult = { exito, mensajeFinal ->
                    if (exito) {
                        // Esto se ejecuta al terminar todo o si ocurre un error fatal
                        // Puedes ocultar el ProgressBar aquí
                        showToast(mensajeFinal)
                    } else {
                        showToast(mensajeFinal)
                    }
                }
            )
        }
    }

    //funcion para obtener el usuario
    fun obtenerUser(){
        lifecycleScope.launch{
            val usrSess = loginUserDao.obtenerUsuario(usuario.toString())?.usuario.toString()
            tvUser.text = usrSess
        }


    }

    // Función auxiliar para no repetir código de UI

    private fun actualizarInterfaz(iniciar: Boolean, terminar: Boolean) {
        btnIniDia.isEnabled = iniciar
        btnIniDia.alpha = if (iniciar) 1.0f else 0.5f
        
        btnFinDia.isEnabled = terminar
        btnFinDia.alpha = if (terminar) 1.0f else 0.5f
    }

    //funcion para mostrar un mensaje toast
    private fun showToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}