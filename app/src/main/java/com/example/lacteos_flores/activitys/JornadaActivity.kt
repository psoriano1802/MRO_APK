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
        tvFolio = findViewById(R.id.tv_folio)
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
        // 1. Obtenemos la ubicación con tu nueva clase
        gpsHelper.obtenerUbicacionActual(
            onSuccess = { lat, lon, address ->

                // 2. Llenamos la Data Class con los datos obtenidos
                val requestData = ubicacionRequest(
                    login = Login(user = usuario.toString(),pass = pass.toString()),
                    li = lat.toString(),
                    lo = lon.toString(),
                    dire = address,
                    nom = "Vendedor 2",
                    lf = lat.toString(),
                    lof = lon.toString(),
                    diref = address

                )
                System.out.println("requestData:"+requestData)
                // 3. Enviamos los datos usando Retrofit
                enviarDatosAlServidor(requestData,tipo)
            },
            onError = { mensajeError ->
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
            }
        )
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
                                // En tu modelo TerDia, 'err' está mapeado a 'Fecha_Termina'
                               showToast("Error: ${item?.err}")
                            }
                        }
                    }
                }catch (e: Exception){

                    showToast("Error: ${e.message}")
                }
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