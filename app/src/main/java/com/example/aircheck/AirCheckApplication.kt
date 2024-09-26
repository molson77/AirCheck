package com.example.aircheck

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AirCheckApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}