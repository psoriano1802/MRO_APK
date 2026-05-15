package com.example.lacteos_flores.activitys

import android.Manifest
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
    private lateinit var tvFolio: TextView
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

        // Inicializar views
       initViews()
        // obtenemmos fecha actual
        fecha()
        // Configurar listeners de botones
        setupButtonListeners()
    }

    //funcion para inicializar los views
    private fun initViews() {
        //textviews

        tvFecha = findViewById(R.id.tv_fecha)

        //botones
        btnIniDia = findViewById(R.id.btn_ini_dia)
        btnFinDia = findViewById(R.id.btn_fin_dia)
        btnMenu = findViewById(R.id.btn_menu)
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
            verificarPermisosYEjecutar(TipoJornada.FIN)
        }
        btnMenu.setOnClickListener {
            //regresa al menu principal
            finish()
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
            // Asumiendo que obtenerUsuario es una función que busca por el ID/User
            val datosUsuario = loginUserDao.obtenerUsuario(usuario.toString())

            // Extraemos los datos que necesites (ejemplo: nombre, almacen, etc.)
            //val nombreUsuario = datosUsuario?. ?: "Usuario Desconocido"
            val almacenUsuario = datosUsuario?.almacen ?: ""

            // 2. Procedemos con la ubicación
            gpsHelper.obtenerUbicacionActual(
                onSuccess = { lat, lon, address ->
                    // 3. Construimos el request con los datos de la DB y el GPS
                    val requestData = ubicacionRequest(
                        login = Login(user = usuario.toString(), pass = pass.toString()),
                        li = lat.toString(),
                        lo = lon.toString(),
                        dire = address,
                        nom = almacenUsuario, // <--- Dato obtenido de la tabla Usuario
                        lf = lat.toString(),
                        lof = lon.toString(),
                        diref = address
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
        // Dentro de enviarDatosAlServidor
        val jsonEnviado = com.google.gson.Gson().toJson(request)
        println("DEBUG JSON ENVIADO: $jsonEnviado")
            // Aquí puedes implementar la lógica para guardar los datos
            lifecycleScope.launch {
                try {
                    if(tipo == TipoJornada.INICIO){
                        val response = RetrofitClient.apiService.sendIniDia(request)
                        if (response.isSuccessful){
                            val res = response.body()
                            val item = res?.IniciaDiaResponse?.getOrNull(0)
                            if (item?.ok.equals("1")){
                                showToast( item?.msn ?: "Jornada Iniciada")
                                actualizarInterfaz(true)
                                showToast("Comenzando la sincronizacion de catalogos...")
                                sincronizarCatalogos()
                            }else{
                                showToast(item?.msn ?: "Error al iniciar la jornada")
                            }
                        }
                    }else{
                        // 2. Manejo para FIN (Usa el modelo TerminaDiaResponse)
                        val response = RetrofitClient.apiService.sendTerDia(request)
                        if (response.isSuccessful) {
                            val body = response.body()
                            val item = body?.TerminaDiaResponse?.getOrNull(0)
                            if (item?.ok == "1") {
                                showToast(item.msn ?: "Jornada Terminada")
                                finish() // Cerramos la actividad al terminar
                            } else {
                               showToast("Error: ${item?.err}")
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


    // Función auxiliar para no repetir código de UI

    private fun actualizarInterfaz(inicioExitoso: Boolean) {
        if (inicioExitoso) {
            btnIniDia.isEnabled = false
            btnIniDia.alpha = 0.5f
            btnFinDia.isEnabled = true
            btnFinDia.alpha = 1.0f
        }
    }

    //funcion para mostrar un mensaje toast
    private fun showToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}