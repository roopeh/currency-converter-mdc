package com.example.user.currencyconverter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), BackgroundThread.ThreadNotifier
{
    private val latestCurrencies = "https://api.exchangeratesapi.io/latest"

    private val TAG_DEBUG = "MDC_PROJECT"

    private val TAG_DATE = "date"
    private val TAG_RATES = "rates"

    // Hash map for exchange rates
    private var ratesList: HashMap<String, Double> = HashMap()

    // UI
    private var selectedSpinner: Spinner? = null
    private var convertSpinner: Spinner? = null

    private var selectedImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create toolbar menu
        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbarMenu)
        setSupportActionBar(toolbar)

        // Load UI widgets
        selectedSpinner = findViewById(R.id.spinnerSelectedCurrency)
        convertSpinner = findViewById(R.id.spinnerConvertedCurrency)
        selectedImage = findViewById(R.id.imageSelectedCurrency)

        // API data is updated once every day so fetch new data only if the date has changed to save bandwidth
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

        // Populate spinners with data
        populateSpinners()
    }

    private fun populateSpinners()
    {
        val countryList = ArrayList<String>()
        for ((key, _) in ratesList)
        {
            //countryList.add(getCountryName(applicationContext, key))
            countryList.add(key)
        }
        convertSpinner?.adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, countryList)

        // Add user's saved selection to selectable list
        // todo: missing default selection
        countryList.add("EUR")
        selectedSpinner?.adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, countryList)


        selectedSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                val string = ("flag_" + parent?.getItemAtPosition(position)).toLowerCase()
                val testid = resources.getIdentifier(string, "drawable", packageName)
                // if id == 0 -> choose default
                Log.d(TAG_DEBUG, "Test string: " + string + ", id: " + testid)
                selectedImage?.setImageResource(testid)
            }
        }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean
    {
        when (item.itemId)
        {
            R.id.action_change_base_currency ->
            {
                Log.d(TAG_DEBUG, "Clicked overflow menu")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestDone(data: JSONObject) { runOnUiThread { saveToApplicationCache(data) } }
}
