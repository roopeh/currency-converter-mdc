package com.example.user.currencyconverter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), BackgroundThread.ThreadNotifier
{
    private val latestCurrencies = "https://api.exchangeratesapi.io/latest"

    private val TAG_DEBUG = "MDC_PROJECT"

    private val TAG_DATE = "date"
    private val TAG_RATES = "rates"

    private var ratesList: HashMap<String, Double> = HashMap()

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
        val listType = object: TypeToken<HashMap<String, Double>>(){}.type
        ratesList = Gson().fromJson<HashMap<String, Double>>(rates, listType)
        Log.d(TAG_DEBUG, ratesList.toString())
    }

    private fun saveToApplicationCache(json: JSONObject)
    {
        // Process rates from JSONObject to string with gson
        val ratesJson = json.getJSONObject(TAG_RATES)
        val tmpList: HashMap<String, Double> = HashMap()
        for (i in 0 until ratesJson.names().length())
        {
            tmpList[ratesJson.names().getString(i)] = ratesJson.getDouble(ratesJson.names().getString(i))
        }
        val ratesString = Gson().toJson(tmpList)

        // Save date and rates to application's cache
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(TAG_DATE, json.getString(TAG_DATE))
        editor.putString(TAG_RATES, ratesString)
        editor.apply()

        processRatesFromString(ratesString)
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
