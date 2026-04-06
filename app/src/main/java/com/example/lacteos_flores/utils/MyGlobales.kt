package com.example.lacteos_flores.utils

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Globales.init(this)
    }
}