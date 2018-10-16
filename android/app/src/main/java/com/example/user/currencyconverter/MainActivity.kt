package com.example.user.currencyconverter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity()
{
    private val latestCurrencies = "https://api.exchangeratesapi.io/latest"

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load latest currencies to cache in start up in background thread
        val currencyThread = BackgroundThread(latestCurrencies)
        currencyThread.start()
    }
}
