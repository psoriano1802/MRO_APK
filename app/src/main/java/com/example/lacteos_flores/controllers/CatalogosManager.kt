package com.example.lacteos_flores.controllers

import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.BancoEntity
import com.example.lacteos_flores.data.DoctosEntity
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.bancos
import com.example.lacteos_flores.models.documentos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson

class CatalogosManager(private val db: AppDatabase) {

    private val gson = Gson()

    suspend fun sincronizarTodos(
        login: Login,
        onProgress: (String) -> Unit,
        onResult: (Boolean, String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            // Contadores y registros para el reporte final
            var totalErrores = 0
            val reporteErrores = mutableListOf<String>()

            // ---------------------------------------------------------
            // 1. Sincronizar Documentos
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Documentos...") }
                sincronizarDocumentos(login)
                withContext(Dispatchers.Main) { onProgress("✅ Documentos actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Documentos: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Documentos") }
            }

            // ---------------------------------------------------------
            // 2. Sincronizar Artículos (Ejemplo)
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Bancos...") }
                sincronizarBancos(login) // Tu función real aquí
                withContext(Dispatchers.Main) { onProgress("✅ Bancos Actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Artículos: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Bancos") }
            }

            // ---------------------------------------------------------
            // 3. Sincronizar Proveedores (Ejemplo)
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Proveedores...") }
                // sincronizarProveedores(login) // Tu función real aquí
                withContext(Dispatchers.Main) { onProgress("✅ Proveedores actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Proveedores: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Proveedores") }
            }

            // ---------------------------------------------------------
            // EVALUACIÓN FINAL
            // ---------------------------------------------------------
            withContext(Dispatchers.Main) {
                if (totalErrores == 0) {
                    // Todo salió perfecto
                    onResult(true, "Todos los catálogos se sincronizaron correctamente.")
                } else {
                    // Terminó, pero hubo fallos. Unimos la lista de errores para mostrarlos.
                    val mensajeFallo = "Sincronización finalizada con $totalErrores error(es):\n" +
                            reporteErrores.joinToString("\n")

                    // Pasamos 'false' para que la Activity sepa que no fue una sincronización limpia
                    onResult(false, mensajeFallo)
                }
            }
        }
    }

    // Tus funciones privadas (sincronizarDocumentos, etc.) se quedan exactamente igual
    // ya que ellas se encargan de lanzar las excepciones si algo sale mal con la red o el JSON.
    // Tu lógica original, ahora convertida en una función privada e independiente
    private suspend fun sincronizarDocumentos(login: Login) {
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getDoctos(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor Documentos: ${response.code()}")
        }

        val listaRaw = response.body()?.ResponseDocumentos ?: throw Exception("Respuesta vacía de Documentos")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<DoctosEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val doc = gson.fromJson(jsonElement, documentos::class.java)

                    listaParaGuardar.add(DoctosEntity(
                        gen = doc.gen.toString(),
                        nat = doc.nat.toString(),
                        grp = doc.grp.toString(),
                        tipo = doc.tipo.toString(),
                        descripcion = doc.descrp.toString(),
                        isr = doc.isr.toString(),
                        iva = doc.iva.toString(),
                        retenido = doc.ret.toString()
                    ))
                }

                db.doctosDao().eliminarTodo()
                db.doctosDao().insertarDocumentos(listaParaGuardar)
            } else {
                throw Exception("El WS de Documentos no devolvió ok:1")
            }
        }
    }

    private suspend fun sincronizarBancos(login: Login) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getBancos(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor bancos: ${response.code()}")
        }

        val listaRaw = response.body()?.BancosResponse ?: throw Exception("Respuesta vacía de Bancos")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<BancoEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val banco = gson.fromJson(jsonElement, bancos::class.java)

                    listaParaGuardar.add(BancoEntity(
                        clave = banco.cve.toString(),
                        banco = banco.ban.toString()
                    ))
                }

                db.bancoDao().eliminarTodo()
                db.bancoDao().insertarBanco(listaParaGuardar)
            } else {
                throw Exception("El WS de bancos no devolvió ok:1")
            }
        }
    }

}