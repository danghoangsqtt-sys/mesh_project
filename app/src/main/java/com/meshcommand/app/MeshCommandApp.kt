package com.meshcommand.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MeshCommandApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
