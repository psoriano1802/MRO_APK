package com.example.agroag_mro.activitys

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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agroag_mro.R
import com.example.agroag_mro.adapters.MenuOptions
import com.example.agroag_mro.adapters.OrdenesAdapter
import com.example.agroag_mro.data.AppDatabase
import com.example.agroag_mro.data.PantallasEntity
import com.example.agroag_mro.data.UsuarioDao
import com.example.agroag_mro.interfaz.RetrofitClient
import com.example.agroag_mro.models.Login
import com.example.agroag_mro.models.LoginRequest
import com.example.agroag_mro.models.OrdenItem
import com.example.agroag_mro.models.OrdenesRequest
import com.example.agroag_mro.models.SucursalItem
import com.example.agroag_mro.models.SucursalResponse
import com.example.agroag_mro.models.TipoTrabajos
import com.example.agroag_mro.models.itemScreen
import com.example.agroag_mro.utils.Prefs
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ListaOrdenesActivity : AppCompatActivity() {
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
    private lateinit var sucursales: List<SucursalItem>
    private var sucursalID: String? = null
    private var sucursalUsurio: String? = null

    private val listaOrdenes = mutableListOf<OrdenItem>()

    private val listaPantallas = mutableListOf<PantallasEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_ordenes)

        etSucursal = findViewById(R.id.spinnerSucursal)
        etfechaFinal = findViewById(R.id.etFechaFinal)
        etfechaInicial = findViewById(R.id.etFechaInicial)
        checkAtrasadas = findViewById(R.id.checkAtrasadas)
        btnBuscar = findViewById(R.id.btnBuscar)
        recyclerView = findViewById(R.id.rvOrdenes)
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
                val request = LoginRequest(Login(usuario.toString(), pass.toString()))
                val response = RetrofitClient.apiService.getPantallas(request)
                if (response.isSuccessful) {
                    val pantallas = response.body()
                    pantallas?.let { items ->
                        val okItem = items.ResponsePantallas.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            listaPantallas.clear() // limpiar antes de llenar
                           items.ResponsePantallas.forEach {
                               //filtramos solo los que tienen el name != null


                           }
                            System.out.println("Pantllas"+listaPantallas)
                        }
                    }

                }
            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@ListaOrdenesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                val listScreenUser = loginUserDao.obtenerPantallas(usuario?.usuario.toString())
                System.out.println("listScreenUser:"+listScreenUser)
                listaPantallas.clear()
                listaPantallas.addAll(listScreenUser)

            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@ListaOrdenesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                val request = LoginRequest(Login(usuario.toString(), pass.toString()))
                System.out.println("request:"+request)
                val response = RetrofitClient.apiService.getSucursales(request)
                System.out.println("response:"+response)
                if (response.isSuccessful) {
                    val sucursalesRes = response.body()
                    System.out.println("sucursales:" + sucursalesRes)
                    sucursalesRes?.let { items ->
                        val okItem = items.ResponseSucursal.find { it.ok != null }
                        if (okItem?.ok == "1"){
                            setupSpinnerSucursal(items,sucursalUsurio)
                        }

                    }
                }
            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@ListaOrdenesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }
    //funcion para configurar el spinner de sucursales
    private fun setupSpinnerSucursal(response: SucursalResponse, sucursalGuardada: String? = null ){
        sucursales = response.ResponseSucursal.filter { it.cve != null }
        val spSucursales = sucursales.map { it.suc }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spSucursales)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        etSucursal.adapter = adapter

        //si el usuario tiene una sucursal asignada se dejara fija la sucursal
        sucursalGuardada?.let { guardada ->
            val index = sucursales.indexOfFirst { it.cve == guardada }
            if (index >= 0) {
                etSucursal.isEnabled = false
                etSucursal.setSelection(index)
                sucursalID = sucursales[index].cve.toString()
            }
        }

        etSucursal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccionada = sucursales[position]
                System.out.println("sucursal seleccionada:"+seleccionada.cve)
                sucursalID = seleccionada.cve.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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

                val fechaInicial = etfechaInicial.text.toString()
                val fechaFinal = etfechaFinal.text.toString()
                val atrasadas = if (checkAtrasadas.isChecked) "S" else "N"

                //mandamos el request de OrdenesRequest
                val request = OrdenesRequest(Login(usuario.toString(), pass.toString()), fechaInicial, fechaFinal,
                    sucursalID.toString(), atrasadas)
                System.out.println("request:"+request)
                val response = RetrofitClient.apiService.getOrdenes(request)
                System.out.println("response:"+response)
                if (response.isSuccessful) {

                    val ordenes = response.body()
                    System.out.println("ordenes:"+ordenes)
                    ordenes?.let { items ->
                        val okItem = items.ResponseOrdenesAsig.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            listaOrdenes.clear()
                            val ordenesItems = items.ResponseOrdenesAsig.filter { it.folio != null }

                            for (orden in ordenesItems) {
                                //ord.add(OrdenItem(null,orden.folio!!,null, orden.nomAct!!,null, orden.tipo!!,null,null,null,null,null,null,null,null,null))
                                listaOrdenes.add(OrdenItem(null,orden.folio!!,orden.activos!!, orden.nomAct!!,orden.suc!!, orden.tipo!!,null,null,orden.cc!!,orden.gen!!,orden.nat!!,orden.grp!!,orden.tip!!,null,orden.paq!!,null))
                            }
                           // listaOrdenes.addAll(ord)
                            System.out.println("listaOrdenes:"+listaOrdenes)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }



            }catch (e: Exception){
                System.out.println("error:"+e)
                Toast.makeText(this@ListaOrdenesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }


        adapter.notifyDataSetChanged()
    }

    private fun mostrarMensajeSinPermiso(accion: String) {
        Toast.makeText(this, "No tienes permiso para $accion esta orden", Toast.LENGTH_SHORT).show()
    }
}