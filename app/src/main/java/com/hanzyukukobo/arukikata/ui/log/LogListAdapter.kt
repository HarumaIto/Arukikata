package com.hanzyukukobo.arukikata.ui.log

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.hanzyukukobo.arukikata.R

data class LogListItem(
    val name: String,
    val date: String
)

class LogListAdapter constructor(
    context: Context,
    private val resource: Int,
    private val items: List<LogListItem>
) : ArrayAdapter<LogListItem>(context, resource, items) {

    private var inflater: LayoutInflater

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(resource, null)
        val item = items[position]

        val nameText = view.findViewById(R.id.name) as TextView
        nameText.text = item.name

        val dateText = view.findViewById(R.id.date) as TextView
        dateText.text = item.date

        return view
    }
}