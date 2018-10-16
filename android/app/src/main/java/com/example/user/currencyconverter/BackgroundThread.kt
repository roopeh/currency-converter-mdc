package com.example.user.currencyconverter

import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class BackgroundThread(url: String, listener: ThreadNotifier) : Thread()
{
    interface ThreadNotifier { fun onRequestDone(data: JSONObject) }

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

        // Return data as JSONObject
        var jsonObject: JSONObject? = null
        try { jsonObject = JSONObject(results.toString()) }
        catch (e : JSONException) { e.printStackTrace() }
        mListener?.onRequestDone(jsonObject!!)

        // Close connections
        input.close()
        urlConnection.disconnect()
    }
}