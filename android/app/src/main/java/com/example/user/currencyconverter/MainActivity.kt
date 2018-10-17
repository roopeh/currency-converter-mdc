package com.example.user.currencyconverter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.text.DecimalFormat
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
    private var convertedImage: ImageView? = null

    private var selectedCurrency: EditText? = null
    private var convertedCurrency: TextView? = null

    private var buttonConvert: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load UI widgets
        selectedSpinner = findViewById(R.id.spinnerSelectedCurrency)
        convertSpinner = findViewById(R.id.spinnerConvertedCurrency)
        selectedImage = findViewById(R.id.imageSelectedCurrency)
        convertedImage = findViewById(R.id.imageConvertCurrency)
        selectedCurrency = findViewById(R.id.textCurrencyAmount)
        convertedCurrency = findViewById(R.id.textConvertedAmount)
        buttonConvert = findViewById(R.id.buttonConvert)

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

        // Set button listeners
        buttonConvert?.setOnClickListener { convertAmount() }
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
        val countryListFrom = ArrayList<String>()
        val countryListTo = ArrayList<String>()
        for ((key, _) in ratesList)
        {
            countryListFrom.add(key)
            countryListTo.add(key)
        }
        var arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, countryListTo)
        convertSpinner?.adapter = arrayAdapter

        // Add user's previous selection to selectable list
        // API won't list previous selection so we have to manually add it
        // todo: do this properly
        countryListFrom.add("EUR")
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countryListFrom)
        selectedSpinner?.adapter = arrayAdapter
        val position = arrayAdapter.getPosition("EUR")
        selectedSpinner?.setSelection(position)

        // Set listeners
        convertSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                // Change flag icon to selected currency
                val flagName = ("flag_" + parent?.getItemAtPosition(position)).toLowerCase()
                val imageId = resources.getIdentifier(flagName, "drawable", packageName)
                // todo: if id == 0 -> choose default
                convertedImage?.setImageResource(imageId)
            }
        }
        selectedSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                // Change flag icon to selected currency
                val string = ("flag_" + parent?.getItemAtPosition(position)).toLowerCase()
                val testid = resources.getIdentifier(string, "drawable", packageName)
                // todo: if id == 0 -> choose default
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

    private fun convertAmount()
    {
        val convertToCurrency = convertSpinner?.selectedItem as String
        val rate = ratesList[convertToCurrency]!!

        // Get amount to be converted
        val convertAmount = (selectedCurrency?.text.toString()).toDouble()
        // Calculate
        val finalAmount = convertAmount * rate
        // Put amount in text view
        val decimalFormat = DecimalFormat("0.00")
        convertedCurrency?.text = decimalFormat.format(finalAmount).toString()
    }

    private fun getDateAsString() : String
    {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        return ("" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH))
    }

    override fun onRequestDone(data: JSONObject) { runOnUiThread { saveToApplicationCache(data) } }
}
