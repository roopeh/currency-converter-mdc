package com.example.user.currencyconverter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity(), BackgroundThread.ThreadNotifier
{
    private val latestCurrencies = "https://api.exchangeratesapi.io/latest"

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load latest currencies to cache in start up in background thread
        val currencyThread = BackgroundThread(latestCurrencies, this)
        currencyThread.start()
    }

    override fun onRequestDone(data: String)
    {
        Log.d("MDC_PROJECT", "MainActivity: " + data)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
