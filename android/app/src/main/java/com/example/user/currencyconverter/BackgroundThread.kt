package com.example.user.currencyconverter

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class BackgroundThread(url: String) : Thread()
{
    private var mUrl: String? = null

    init
    {
        mUrl = url
    }

    override fun run()
    {
        val urlConnection: HttpURLConnection = URL(mUrl).openConnection() as HttpURLConnection
        val input = BufferedInputStream(urlConnection.inputStream)

        // Process data
        val reader = BufferedReader(InputStreamReader(input))
        var currentLine: String? = reader.readLine()
        val results = StringBuilder()
        while (currentLine != null)
        {
            results.append(currentLine)
            results.append(System.getProperty("line.separator"))
            currentLine = reader.readLine()
        }

        // Put data in string format
        val resultData = results.toString()
        Log.d("MDC_PROJECT", "Data: " + resultData)

        // Close connections
        input.close()
        urlConnection.disconnect()
    }
}