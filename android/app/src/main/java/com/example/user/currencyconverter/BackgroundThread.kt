package com.example.user.currencyconverter

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class BackgroundThread(url: String, listener: ThreadNotifier) : Thread()
{
    interface ThreadNotifier { fun onRequestDone(data: String) }

    private var mUrl: String? = null
    private var mListener: ThreadNotifier? = null

    init
    {
        mUrl = url
        mListener = listener
    }

    override fun run()
    {
        // Initialize connection
        val urlConnection: HttpURLConnection = (URL(mUrl)).openConnection() as HttpURLConnection
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

        // Return data in string format
        mListener?.onRequestDone(results.toString())

        // Close connections
        input.close()
        urlConnection.disconnect()
    }
}