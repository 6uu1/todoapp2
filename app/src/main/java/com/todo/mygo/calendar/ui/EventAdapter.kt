package com.todo.mygo.calendar.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.mygo.R // Assuming you will create item_event.xml
import com.todo.mygo.calendar.data.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(
    private val onItemClicked: (Event) -> Unit,
    private val onDeleteClicked: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false) // Use the new item_event.xml
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event, timeFormat)
        holder.itemView.setOnClickListener { onItemClicked(event) }
        holder.deleteButton.setOnClickListener { onDeleteClicked(event) }
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.eventTitleTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.eventTimeTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteEventButton) // Made public for adapter access

        fun bind(event: Event, timeFormat: SimpleDateFormat) {
            titleTextView.text = event.title
            val startTime = timeFormat.format(Date(event.startTime))
            val endTime = timeFormat.format(Date(event.endTime))
            timeTextView.text = "$startTime - $endTime"
            // You can add more visual cues here, e.g., based on priority
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