package com.example.lacteos_flores.utils

import android.app.Application
import com.example.lacteos_flores.interfaz.AppwriteHelper

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Globales.init(this)
        AppwriteHelper.init(this)
    }
}