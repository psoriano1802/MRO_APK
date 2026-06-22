package com.example.lacteos_flores.interfaz

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppwriteHelper {
    private lateinit var client: Client
    lateinit var account: Account

    fun init(context: Context) {
        client = Client(context)
            .setEndpoint("https://fra.cloud.appwrite.io/v1")
            .setProject("6a34c7100029653e9d35")

        account = Account(client)
        
        // Ping Appwrite to verify connectivity
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Assuming ping() is available as per instructions
                // If it's a suspend function or requires specific handling, we'll see after build
                // The instructions say: client.ping()
                client.ping()
                println("Appwrite: Ping successful")
            } catch (e: Exception) {
                println("Appwrite: Ping failed: ${e.message}")
            }
        }
    }
}
