package com.example.lacteos_flores.activitys

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.adapters.OrdenesAdapter
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.PantallasEntity
import com.example.lacteos_flores.data.UsuarioDao
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.OrdenItem
import com.example.lacteos_flores.models.OrdenesRequest
import com.example.lacteos_flores.utils.Prefs
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VentasActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var etSucursal: Spinner
    private lateinit var adapter: OrdenesAdapter
    private lateinit var etfechaFinal: EditText
    private lateinit var etfechaInicial: EditText
    private lateinit var checkAtrasadas: CheckBox
    private lateinit var btnBuscar: Button
    //variables para base de datos
    private lateinit var db: AppDatabase
    private lateinit var loginUserDao: UsuarioDao
    //vairables locales
    private var usuario: String? = null
    private var pass: String? = null
    private var sucursalID: String? = null
    private var sucursalUsurio: String? = null

    private val listaOrdenes = mutableListOf<OrdenItem>()

    private val listaPantallas = mutableListOf<PantallasEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas)



        recyclerView.layoutManager = LinearLayoutManager(this)


        Inicializa()
        setupListeners()
    }

    private fun Inicializa(){
        val fecAct = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        usuario = Prefs(this).obtenerUsuario().first.toString()
        pass = Prefs(this).obtenerUsuario().second.toString()
        obtenerDatosUsuario()
        obetenerSucursalesWS()
        //obtenerPantallasWS()
        etfechaFinal.setText(fecAct)
        etfechaInicial.setText(fecAct)
        //evento para abrir el fdate picker
        etfechaFinal.setOnClickListener {
            showDatePicker(etfechaFinal)
        }
        etfechaInicial.setOnClickListener {
            showDatePicker(etfechaInicial)
        }
        accionesAddapter()


    }
    //funciones para validar permisos
    private fun accionesAddapter(){
        adapter = OrdenesAdapter(
            listaOrdenes,
            onVerClick = { item ->
                if(tienePermiso("REFACCIONES")){
                    val intent = Intent(this, SolicitaRefaccionActivity::class.java)
                    intent.putExtra("ORDEN_ITEM", item)
                    startActivity(intent)
                }else{
                    mostrarMensajeSinPermiso("REFACCIONES")
                }
            },
            onEditarClick = { item ->
                if(tienePermiso("MANOOBRA")){
                    val intent = Intent(this, ManoObraActivity::class.java)
                    intent.putExtra("ORDEN_ITEM", item)
                    startActivity(intent)
                }else{
                    mostrarMensajeSinPermiso("MANOOBRA")
                }
            },
            onEliminarClick = { item -> Toast.makeText(this, "Registra Activo: ${item.folio}", Toast.LENGTH_SHORT).show() }
        )

        recyclerView.adapter = adapter
    }
    private fun tienePermiso(accion: String): Boolean {
        return listaPantallas.any { it.pantalla == accion }
    }

    //funcion para obtener las pantallas desde el servidor, que retorna una lista de pantallas
    private fun obtenerPantallasWS(){
        lifecycleScope.launch {
            try {

            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@VentasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerDatosUsuario() {
        db = AppDatabase.getDatabase(this)
        loginUserDao = db.usuarioDao()
        lifecycleScope.launch {
            try {
                val usuario = loginUserDao.obtenerUsuario(usuario.toString())
                sucursalUsurio = usuario?.sucursal


            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@VentasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }

    }
    private fun setupListeners() {
        btnBuscar.setOnClickListener {
            // 🚀 Aquí simulamos cargar datos del WS
            cargarDatosDesdeWS()
        }

    }
    private fun obetenerSucursalesWS() {
        lifecycleScope.launch {
            try {

            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@VentasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

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
    private fun cargarDatosDesdeWS() {
       //mandamos a llamar la informacion de las ordenes desde el ws
        listaOrdenes.clear()
        lifecycleScope.launch {
            try {


            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@VentasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }


        adapter.notifyDataSetChanged()
    }

    private fun mostrarMensajeSinPermiso(accion: String) {
        Toast.makeText(this, "No tienes permiso para $accion esta orden", Toast.LENGTH_SHORT).show()
    }
}