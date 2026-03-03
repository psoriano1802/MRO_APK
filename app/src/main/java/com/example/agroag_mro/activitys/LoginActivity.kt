package com.example.agroag_mro.activitys

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agroag_mro.databinding.ActivityLoginBinding
import com.example.agroag_mro.models.LoginRequest
import com.example.agroag_mro.models.Login
import com.example.agroag_mro.models.LoginResponse
import com.example.agroag_mro.interfaz.RetrofitClient
import com.example.agroag_mro.utils.Prefs
import com.example.agroag_mro.data.AppDatabase
import com.example.agroag_mro.data.UsuarioEntity
import com.google.mlkit.vision.face.*
import com.google.mlkit.vision.common.InputImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.agroag_mro.data.PantallasEntity
import com.example.agroag_mro.data.UsuarioDao
import com.example.agroag_mro.utils.Globales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: Prefs
    //variables para la base de datos
    private lateinit var loginUserDao: UsuarioDao
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)
        db = AppDatabase.getDatabase(this)
        loginUserDao = db.usuarioDao()

        checkPermissions()

        binding.btnIngresar.setOnClickListener {
            val user = binding.etUsuario.text.toString()
            val pass = binding.etPassword.text.toString()
            if (user.isNotEmpty() && pass.isNotEmpty()) {
                loginManual(user, pass)
            } else {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun loginManual(user: String, pass: String) {
        //bloqueamos el boton para que solos eejecute una sola vez

        lifecycleScope.launch {
            val isValid= validaUsuario(user, pass)
            System.out.println("isValid:"+isValid)
            if (isValid) {
                binding.btnIngresar.isEnabled = false
                navigateToMenuPrincipal()
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Usuario o contraseña incorrectas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    //funcion que enviaralapeticion al ws de login
    private suspend fun validaUsuario(user: String, pass: String): Boolean = withContext(Dispatchers.IO) {

        try{
            val request = LoginRequest(Login(user, pass))
            System.out.println("request:"+request)
            val response = RetrofitClient.apiService.login(request)
            System.out.println("response:"+response)

            if(response.isSuccessful){
                val res = response.body()
                System.out.println("res:"+res)
                res?.LoginResponse?.let { items ->
                    val okItem = items.find { it.ok != null }
                    if (okItem?.ok == "1") {



                        //guardamos el usuario con persitensia
                        val usuario = items.find { it.User != null }
                        val cvesuc = items.find { it.NoSucursal != null }
                        val sucursal = items.find { it.Sucursal != null }
                        val cvealm = items.find { it.NoAlmacen != null }
                        val alm = items.find { it.Almacen != null }

                        //por validar si se reuqiere ocultar o solo bloquear
                        //loginUserDao.eliminarPantallas(user)

                        val usuarioEntity = UsuarioEntity(
                            usuario = usuario?.User ?: "",
                            sucursal = usuario?.Sucursal ?: "",
                            cve_suc = usuario?.NoSucursal?: "",
                            cve_alma = usuario?.NoAlmacen ?: "",
                            almacen = usuario?.Almacen ?:"")
                        System.out.println("pantallas:"+usuarioEntity)
                        prefs.guardarUsuario(usuario?.User ?: "", pass, "")
                        Globales.usuario = usuario?.User
                        Globales.password = pass

                        /* for (item in accesos?.accesos ?: emptyList()) {
                            val pantalla = item.pantalla ?: ""
                            val pantallaEntity = PantallasEntity(
                                usuario = usuario?.usuario ?: "",
                                pantalla = pantalla,
                                acceso = true)
                            System.out.println("pantallaEntity:"+pantallaEntity)
                            loginUserDao.insertarPantallas(pantallaEntity)

                        }*/


                        loginUserDao.insertar(usuarioEntity)

                        return@withContext true
                    } else {

                        return@withContext false
                    }
                }
            }
            false
        }catch (e: Exception){
            withContext(Dispatchers.Main) {
                System.out.println("Error de conexion:"+e)
                Toast.makeText(this@LoginActivity, "Error de conexion", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }


    private fun navigateToMenuPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun startFaceLogin() {
        val intent = Intent(this, FaceDetectionActivity::class.java)
        startActivity(intent)
    }

    private fun checkPermissions() {
        requestPermissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (!result.values.all { it }) {
            Toast.makeText(this, "Permisos necesarios no otorgados", Toast.LENGTH_SHORT).show()
        }
    }
}

