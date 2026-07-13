package com.example.lacteos_flores.activitys

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lacteos_flores.databinding.ActivityLoginBinding
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.utils.Prefs
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.UsuarioEntity
import com.google.mlkit.vision.face.*
import com.example.lacteos_flores.data.UsuarioDao
import com.example.lacteos_flores.utils.Globales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        lifecycleScope.launch {
            val existingUser = loginUserDao.obtenerCualquierUsuario()
            val hasConnection = isNetworkAvailable()

            if (existingUser != null && existingUser.usuario.uppercase() != user.uppercase()) {
                if (hasConnection) {
                    // Si el usuario es diferente y hay internet, preguntamos
                    AlertDialog.Builder(this@LoginActivity)
                        .setTitle("Cambio de Usuario")
                        .setMessage("Se ha detectado un usuario diferente (${existingUser.usuario}). Si ingresas con $user, se borrarán todos los datos locales para sincronizar la nueva cuenta. ¿Deseas continuar?")
                        .setPositiveButton("Sí, borrar y entrar") { _, _ ->
                            lifecycleScope.launch {
                                ejecutarLogin(user, pass, true)
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    // Si no hay internet, no permitimos cambiar de usuario
                    Toast.makeText(
                        this@LoginActivity,
                        "No se puede cambiar a un usuario nuevo sin conexión a internet.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                // Es el mismo usuario o no hay nadie registrado
                ejecutarLogin(user, pass, false)
            }
        }
    }

    private suspend fun ejecutarLogin(user: String, pass: String, shouldClear: Boolean) {
        val isValid = validaUsuario(user, pass, shouldClear)
        if (isValid) {
            binding.btnIngresar.isEnabled = false
            navigateToMenuPrincipal()
        }
    }

    //funcion que enviara la peticion al ws de login
    private suspend fun validaUsuario(user: String, pass: String, shouldClear: Boolean): Boolean = withContext(Dispatchers.IO) {
        val hasConnection = isNetworkAvailable()
        val localUser = loginUserDao.obtenerUsuario(user)

        if (hasConnection) {
            try {
                val request = LoginRequest(Login(user, pass))
                val response = RetrofitClient.apiService.login(request)

                if (response.isSuccessful) {
                    val res = response.body()
                    res?.LoginResponse?.let { items ->
                        val okItem = items.find { it.ok != null }
                        if (okItem?.ok == "1") {

                            // Borramos tablas solo si se confirmó el cambio de usuario
                            if (shouldClear) {
                                System.out.println("Limpiando tablas por cambio de usuario confirmado")
                                db.clearAllTables()
                            }

                            val usuario = items.find { it.User != null }

                            val usuarioEntity = UsuarioEntity(
                                usuario = usuario?.User ?: "",
                                sucursal = usuario?.Sucursal ?: "",
                                cve_suc = usuario?.NoSucursal ?: "",
                                cve_alma = usuario?.NoAlmacen ?: "",
                                almacen = usuario?.Almacen ?: "",
                                pass = pass,
                                lista = usuario?.Lista ?: ""
                            )
                            
                            prefs.guardarUsuario(usuario?.User ?: "", pass, usuario?.Sucursal ?: "")
                            Globales.usuario = usuario?.User
                            Globales.password = pass

                            loginUserDao.insertar(usuarioEntity)
                            return@withContext true
                        }
                    }
                }
            } catch (e: Exception) {
                System.out.println("Error en WS: $e")
            }
        }

        // Validación local (offline o fallo de WS)
        if (localUser != null && localUser.pass == pass) {
            prefs.guardarUsuario(localUser.usuario, localUser.pass, localUser.sucursal)
            Globales.usuario = localUser.usuario
            Globales.password = localUser.pass
            return@withContext true
        }

        withContext(Dispatchers.Main) {
            if (hasConnection) {

                Toast.makeText(this@LoginActivity, "Usuario o contraseña incorrectas", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@LoginActivity, "Sin conexión y credenciales locales no encontradas", Toast.LENGTH_SHORT).show()
            }
        }

        return@withContext false
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
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

