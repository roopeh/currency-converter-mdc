package com.example.user.currencyconverter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
    private val latestCurrencies = "https://api.exchangeratesapi.io/latest?base="

    private val TAG_DEBUG = "MDC_PROJECT"

    private val TAG_DATE = "date"
    private val TAG_RATES = "rates"
    private val TAG_CURRENCY_FROM = "currencyFrom"
    private val TAG_CURRENCY_TO = "currencyTo"

    // Hash map for exchange rates
    private var ratesList: HashMap<String, Double> = HashMap()

    // Selected currencies
    private var currencyFrom: String? = null
    private var currencyTo: String? = null

    // UI
    private var selectedSpinner: Spinner? = null
    private var convertSpinner: Spinner? = null

    private var selectedImage: ImageView? = null
    private var convertedImage: ImageView? = null
    private var switchCurrency: ImageView? = null

    private var selectedCurrency: EditText? = null
    private var convertedCurrency: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load UI widgets
        selectedSpinner = findViewById(R.id.spinnerSelectedCurrency)
        convertSpinner = findViewById(R.id.spinnerConvertedCurrency)
        selectedImage = findViewById(R.id.imageSelectedCurrency)
        convertedImage = findViewById(R.id.imageConvertCurrency)
        switchCurrency = findViewById(R.id.imageSwitchValues)
        selectedCurrency = findViewById(R.id.textCurrencyAmount)
        convertedCurrency = findViewById(R.id.textConvertedAmount)

        // API data is updated once every day so fetch new data only if the date has changed to save bandwidth
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val date = preferences.getString(TAG_DATE, null)
        val rates = preferences.getString(TAG_RATES, null)
        currencyFrom = preferences.getString(TAG_CURRENCY_FROM, null)
        currencyTo = preferences.getString(TAG_CURRENCY_TO, null)
        if (date != getDateAsString())
        {
            // Load latest currencies to cache in start up in background thread
            if (currencyFrom.isNullOrEmpty() || currencyFrom!!.contains("null"))
                currencyFrom = "EUR"
            loadCurrencyJson(currencyFrom as String)
        }
        else
        {
            // should not ever be null here
            assert(rates != null)
            processRatesFromString(rates!!)
        }
    }

    private fun loadCurrencyJson(currency: String)
    {
        // Disable button and text field to avoid possible crash during API load
        //buttonConvert?.isClickable = false
        selectedCurrency?.isEnabled = false

        // Start background thread
        Log.d(TAG_DEBUG, "loadCurrencyJson: $currency")
        val currencyThread = BackgroundThread(latestCurrencies + currency, this)
        currencyThread.start()
    }

    override fun onPause()
    {
        super.onPause()

        // Save current currencies and rates to application cache
        val ratesString = Gson().toJson(ratesList)
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(TAG_RATES, ratesString)
        editor.putString(TAG_CURRENCY_FROM, currencyFrom)
        editor.putString(TAG_CURRENCY_TO, currencyTo)
        editor.apply()

        Log.d(TAG_DEBUG, "Test")
    }

    private fun processRatesFromString(rates: String)
    {
        val listType = object: TypeToken<HashMap<String, Double>>(){}.type
        ratesList = Gson().fromJson<HashMap<String, Double>>(rates, listType)
        Log.d(TAG_DEBUG, ratesList.toString())

        // Populate spinners with data
        populateSpinners()

        // Set listeners to buttons here instead of onCreate to avoid possible crashes (wait for API first)
        selectedCurrency?.isEnabled = true
        selectedCurrency?.addTextChangedListener(object: TextWatcher
        {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { convertAmount() }

            // Empty
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            // Empty
            override fun afterTextChanged(s: Editable?) { }
        })

        // Switches currencies
        switchCurrency?.setOnClickListener {
            val tmpCurrencyFrom = currencyFrom
            currencyFrom = currencyTo
            currencyTo = tmpCurrencyFrom
            loadCurrencyJson(currencyFrom as String)
        }
    }

    private fun populateSpinners()
    {
        // Add each value in rates list to ArrayList
        val countryList = ArrayList<String>()
        for ((key, _) in ratesList) { countryList.add(key) }

        // Default currency is euro
        if (currencyFrom.isNullOrEmpty() || currencyFrom!!.contains("null"))
            currencyFrom = "EUR"

        // Set adapters to spinners
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, countryList)
        selectedSpinner?.adapter = arrayAdapter
        convertSpinner?.adapter = arrayAdapter
        // Set user's chosen currency as selected item
        selectedSpinner?.setSelection(arrayAdapter.getPosition(currencyFrom))
        // Set user's previous selection as selected item, else use first item in list
        if (!currencyTo.isNullOrEmpty() && !currencyTo!!.contains("null"))
            convertSpinner?.setSelection(arrayAdapter.getPosition(currencyTo))

        // Set listeners
        convertSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            // Empty
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                // Change flag icon to selected currency
                val flagName = ("flag_" + parent?.getItemAtPosition(position)).toLowerCase()
                val imageId = resources.getIdentifier(flagName, "drawable", packageName)
                if (imageId != 0)
                {
                    currencyTo = parent?.getItemAtPosition(position).toString()
                    convertedImage?.setImageResource(imageId)
                    convertAmount()
                }
                else
                {
                    // should not happen
                    convertedImage?.setImageResource(android.R.drawable.stat_notify_error)
                    Toast.makeText(applicationContext, "Error in finding flag $flagName", Toast.LENGTH_LONG).show()
                    Log.d(TAG_DEBUG, "Error in finding converted currency flag: Currency: " + parent?.getItemAtPosition(position) + ", flag name: " + flagName)
                }
            }
        }
        selectedSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            // Empty
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                // Change flag icon to selected currency
                val flagName = ("flag_" + parent?.getItemAtPosition(position)).toLowerCase()
                val imageId = resources.getIdentifier(flagName, "drawable", packageName)
                if (imageId != 0)
                {
                    selectedImage?.setImageResource(imageId)

                    // Avoid infinite loop here
                    if (currencyFrom != parent?.getItemAtPosition(position))
                    {
                        currencyFrom = parent?.getItemAtPosition(position).toString()
                        loadCurrencyJson(currencyFrom as String)
                    }
                }
                else
                {
                    // should not happen
                    selectedImage?.setImageResource(android.R.drawable.stat_notify_error)
                    Toast.makeText(applicationContext, "Error in finding flag $flagName", Toast.LENGTH_LONG).show()
                    Log.d(TAG_DEBUG, "Error in finding selected currency flag: Currency: " + parent?.getItemAtPosition(position) + ", flag name: " + flagName)
                }
            }
        }

        // Finally convert amount, if any is given
        convertAmount()
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

        // For some reason API does not add euro to the list if the selected currency is euro
        // looks like its working fine for other currencies
        if (!currencyFrom.isNullOrEmpty() && currencyFrom!!.contains("EUR"))
            tmpList[currencyFrom as String] = 1.0

        // Convert to JSON string so it can be accessed again on app restart
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
        // If value is null, do not proceed
        if (selectedCurrency?.text.isNullOrEmpty())
        {
            convertedCurrency?.text = (0.00).toString()
            return
        }

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
