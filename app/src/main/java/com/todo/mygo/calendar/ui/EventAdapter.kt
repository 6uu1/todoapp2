package com.todo.mygo.calendar.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.mygo.R // Assuming you will create item_event.xml
import com.todo.mygo.calendar.data.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(private val onItemClicked: (Event) -> Unit) :
    ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        // TODO: Replace with actual item_event.xml layout inflation
        // val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        // return EventViewHolder(view)

        // For now, using a simple TextView as a placeholder for the item layout
        val textView = TextView(parent.context)
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.textSize = 16f
        textView.setPadding(16, 16, 16, 16) // In pixels
        return EventViewHolder(textView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
        holder.itemView.setOnClickListener { onItemClicked(event) }
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TODO: Get references to views in item_event.xml if not using a simple TextView
        // private val titleTextView: TextView = itemView.findViewById(R.id.eventItemTitle)
        // private val timeTextView: TextView = itemView.findViewById(R.id.eventItemTime)

        fun bind(event: Event) {
            // This assumes itemView is the TextView itself (placeholder implementation)
            val itemTextView = itemView as TextView

            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startTime = sdfTime.format(Date(event.startTime))
            val endTime = sdfTime.format(Date(event.endTime))

            // TODO: Populate actual views from item_event.xml
            // titleTextView.text = event.title
            // timeTextView.text = "$startTime - $endTime"
            // itemView.setBackgroundColor(if (event.priority > 0) Color.YELLOW else Color.TRANSPARENT) // Example

            itemTextView.text = "${event.title} ($startTime - $endTime)" // Placeholder
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}