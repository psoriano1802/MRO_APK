package com.example.lacteos_flores.activitys

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.adapters.HistorialDocumentosAdapter
import com.example.lacteos_flores.data.AppDatabase
import kotlinx.coroutines.launch

class HistorialDocumentosActivity : AppCompatActivity() {

    private lateinit var rvHistorial: RecyclerView
    private lateinit var adapter: HistorialDocumentosAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_documentos)

        db = AppDatabase.getDatabase(this)
        rvHistorial = findViewById(R.id.rvHistorial)
        rvHistorial.layoutManager = LinearLayoutManager(this)
        
        adapter = HistorialDocumentosAdapter(emptyList())
        rvHistorial.adapter = adapter

        cargarHistorial()
    }

    private fun cargarHistorial() {
        lifecycleScope.launch {
            val historial = db.kdm1Dao().obtenerHistorialDocumentos()
            adapter.updateList(historial)
        }
    }
}
