package com.example.agroag_mro.utils

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Globales.init(this)
    }
}