package com.languagetranslator.translate

import HistoryData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class HistoryAdapter(options: FirebaseRecyclerOptions<HistoryData>) :
    FirebaseRecyclerAdapter<HistoryData, HistoryAdapter.HistoryViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int, model: HistoryData) {
        holder.bind(model)
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sourceTextView: TextView = itemView.findViewById(R.id.source_text_view)
        val translatedTextView: TextView = itemView.findViewById(R.id.translated_text_view)
        val deleteButton = itemView.findViewById<Button>(R.id.delete_button)

        fun bind(favorite: HistoryData) {
            sourceTextView.text = favorite.sourceTxt
            translatedTextView.text = favorite.translatedTxt

            deleteButton.setOnClickListener {
                getRef(bindingAdapterPosition).removeValue()
            }
        }
    }
}
