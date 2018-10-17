package com.example.user.currencyconverter

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RatesActivity : AppCompatActivity()
{
    private val TAG_DEBUG = "MDC_PROJECT"

    private val TAG_CURRENCY_FROM = "currencyFrom"
    private val TAG_RATES = "rates"

    private var currencyList: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rates)

        currencyList = findViewById(R.id.listCurrencies)

        // Get data from intent
        val currency = intent.getStringExtra(TAG_CURRENCY_FROM)
        val ratesString = intent.getStringExtra(TAG_RATES)

        // Set text
        val textCurrency = findViewById<TextView>(R.id.textCurrentCurrency)
        textCurrency.text = getString(R.string.textValue, currency)

        // Set return button
        val buttonReturn = findViewById<Button>(R.id.buttonReturn)
        buttonReturn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Add currencies to list view
        populateListview(ratesString)
    }

    private fun populateListview(data: String)
    {
        val listType = object: TypeToken<HashMap<String, Double>>(){}.type
        val ratesList = Gson().fromJson<HashMap<String, Double>>(data, listType)
        Log.d(TAG_DEBUG, "Data: " + ratesList.toString())

        val arrayList = ArrayList<CurrencyData>()
        for ((currency, value) in ratesList)
        {
            val flagName = ("flag_$currency").toLowerCase()
            var imageId = resources.getIdentifier(flagName, "drawable", packageName)
            if (imageId == 0) imageId = android.R.drawable.ic_delete

            val textDetailed = getDetailedName(currency)
            val textAmount = value.toString()

            val key = CurrencyData(imageId, currency, textDetailed, textAmount)
            arrayList.add(key)
        }

        val arrayAdapter = CurrencyListAdapter(this, arrayList)
        currencyList?.adapter = arrayAdapter
        arrayAdapter.notifyDataSetChanged()
    }

    private fun getDetailedName(currency: String): String
    {
        when(currency)
        {
            "BGN" -> return getString(R.string.BGN)
            "CAD" -> return getString(R.string.CAD)
            "BRL" -> return getString(R.string.BRL)
            "HUF" -> return getString(R.string.HUF)
            "DKK" -> return getString(R.string.DKK)
            "JPY" -> return getString(R.string.JPY)
            "ILS" -> return getString(R.string.ILS)
            "TRY" -> return getString(R.string.TRY)
            "RON" -> return getString(R.string.RON)
            "GBP" -> return getString(R.string.GBP)
            "PHP" -> return getString(R.string.PHP)
            "HRK" -> return getString(R.string.HRK)
            "NOK" -> return getString(R.string.NOK)
            "ZAR" -> return getString(R.string.ZAR)
            "MXN" -> return getString(R.string.MXN)
            "AUD" -> return getString(R.string.AUD)
            "USD" -> return getString(R.string.USD)
            "KRW" -> return getString(R.string.KRW)
            "HKD" -> return getString(R.string.HKD)
            "EUR" -> return getString(R.string.EUR)
            "ISK" -> return getString(R.string.ISK)
            "CZK" -> return getString(R.string.CZK)
            "THB" -> return getString(R.string.THB)
            "MYR" -> return getString(R.string.MYR)
            "NZD" -> return getString(R.string.NZD)
            "PLN" -> return getString(R.string.PLN)
            "CHF" -> return getString(R.string.CHF)
            "SEK" -> return getString(R.string.SEK)
            "CNY" -> return getString(R.string.CNY)
            "SGD" -> return getString(R.string.SGD)
            "INR" -> return getString(R.string.INR)
            "IDR" -> return getString(R.string.IDR)
            "RUB" -> return getString(R.string.RUB)
        }
        return currency
    }
}
