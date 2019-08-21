package com.aaron

import android.app.Application

/**
 * Created by Aaron on 2019-08-21.
 */
class App:Application() {

    companion object {
        lateinit var app:App
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}