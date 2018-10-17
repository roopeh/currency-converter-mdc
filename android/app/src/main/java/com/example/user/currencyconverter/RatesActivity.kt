package com.example.user.currencyconverter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
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
        textCurrency.text = "1 $currency ="

        // Add currencies to list view
        populateListview(ratesString)
    }

    private fun populateListview(data: String)
    {
        val listType = object: TypeToken<HashMap<String, Double>>(){}.type
        val ratesList = Gson().fromJson<HashMap<String, Double>>(data, listType)
        Log.d(TAG_DEBUG, "Data: " + ratesList.toString())

        val arrayList = ArrayList<HashMap<>>()
        for ((currency, value) in ratesList)
        {

        }

        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, arrayList)
        currencyList.adapter = arrayAdapter
    }
}
