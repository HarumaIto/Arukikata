package com.hanzyukukobo.arukikata.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hanzyukukobo.arukikata.R

data class MainCard(
    val title: String,
    val description: String,
    val imageId: Int
)

interface OnClickNextButton {
    fun onNext(mainCard: MainCard)
}

class CardListAdapter constructor(
    private val mainCards: ArrayList<MainCard>,
    private val clickListener: OnClickNextButton
) : RecyclerView.Adapter<CardListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView
        val title: TextView
        val description: TextView
        val button: Button

        init {
            image = view.findViewById(R.id.image)
            title = view.findViewById(R.id.cardTitle)
            description = view.findViewById(R.id.cardDescription)
            button = view.findViewById(R.id.button)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mainCards.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = mainCards[position]

        holder.title.text = card.title
        holder.description.text = card.description
        holder.image.setImageResource(card.imageId)
        holder.button.setOnClickListener {
            clickListener.onNext(card)
        }
    }
}