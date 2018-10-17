package com.example.user.currencyconverter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class CurrencyListAdapter(private val context: Context, private val data: ArrayList<CurrencyData>) : BaseAdapter()
{
    private class ViewHolder(row: View?)
    {
        var imageId: ImageView? = null
        var currencyText: TextView? = null
        var currencyTextDetailed: TextView? = null
        var currencyTextAmount: TextView? = null

        init
        {
            this.imageId = row?.findViewById(R.id.listIcon)
            this.currencyText = row?.findViewById(R.id.listTextCurrency)
            this.currencyTextDetailed = row?.findViewById(R.id.listTextCurrencyDetail)
            this.currencyTextAmount = row?.findViewById(R.id.listTextValue)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        val view: View?
        val viewHolder: ViewHolder

        if (convertView == null)
        {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.currencylist, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        }
        else
        {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val curData = data[position]
        viewHolder.imageId?.setImageResource(curData.imageId)
        viewHolder.currencyText?.text = curData.currencyText
        viewHolder.currencyTextDetailed?.text = curData.currencyTextDetailed
        viewHolder.currencyTextAmount?.text = curData.currencyTextAmount

        return view as View
    }

    override fun getItem(position: Int): Any
    {
        return data[position]
    }

    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }

    override fun getCount(): Int
    {
        return data.size
    }
}

class CurrencyData(var imageId: Int, var currencyText: String, var currencyTextDetailed: String, var currencyTextAmount: String)
