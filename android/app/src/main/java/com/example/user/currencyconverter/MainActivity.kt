package com.example.user.currencyconverter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity(), BackgroundThread.ThreadNotifier
{
    private val latestCurrencies = "https://api.exchangeratesapi.io/latest"
    private val DEBUGTAG = "MDC_PROJECT"

    // JSON fields
    private val TAG_DATE = "date"
    private val TAG_RATES = "rates"

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // API data is updated once every day so fetch new data only if the date has changed, to save bandwidth
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val date = preferences.getString(TAG_DATE, "")
        val rates = preferences.getString(TAG_RATES, "")
        if (date != getDateAsString())
        {
            Log.d(DEBUGTAG, "Dates do not match, date: " + date + ", getDate: " + getDateAsString())

            // Load latest currencies to cache in start up in background thread
            val currencyThread = BackgroundThread(latestCurrencies, this)
            currencyThread.start()
        }
        else
        {
            // should not ever be null here
            assert(rates != null)
            processRatesFromString(rates!!)
        }
    }

    private fun processRatesFromString(rates: String)
    {
        Log.d(DEBUGTAG, "JSON: " + rates)
    }

    private fun saveToApplicationCache(json: JSONObject)
    {
        // Save date and rates to application's cache
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(TAG_DATE, json.getString(TAG_DATE))
        editor.putString(TAG_RATES, json.getString(TAG_RATES))
        editor.apply()

        processRatesFromString(json.getString(TAG_RATES))
    }

    private fun getDateAsString() : String
    {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        return ("" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH))
    }

    override fun onRequestDone(data: JSONObject)
    {
        saveToApplicationCache(data)
    }
}
