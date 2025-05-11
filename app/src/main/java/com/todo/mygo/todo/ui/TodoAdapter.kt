package com.todo.mygo.todo.ui

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.mygo.R
import com.todo.mygo.todo.data.TodoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoAdapter(
    private val onItemClicked: (TodoItem) -> Unit,
    private val onToggleCompleted: (TodoItem, Boolean) -> Unit
) : ListAdapter<TodoItem, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClicked, onToggleCompleted)
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_todo_title)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.tv_todo_due_date)
        private val completedCheckBox: CheckBox = itemView.findViewById(R.id.cb_todo_completed)
        private val priorityIndicator: View = itemView.findViewById(R.id.view_priority_indicator)

        fun bind(
            todoItem: TodoItem,
            onItemClicked: (TodoItem) -> Unit,
            onToggleCompleted: (TodoItem, Boolean) -> Unit
        ) {
            titleTextView.text = todoItem.title
            completedCheckBox.isChecked = todoItem.isCompleted

            if (todoItem.isCompleted) {
                titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                dueDateTextView.paintFlags = dueDateTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                titleTextView.paintFlags = titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                dueDateTextView.paintFlags = dueDateTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            if (todoItem.dueDate != null && todoItem.dueDate > 0) {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dueDateTextView.text = "Due: ${sdf.format(Date(todoItem.dueDate))}"
                dueDateTextView.visibility = View.VISIBLE
            } else {
                dueDateTextView.visibility = View.GONE
            }

            when (todoItem.priority) {
                1 -> priorityIndicator.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, android.R.color.holo_red_dark) // High
                2 -> priorityIndicator.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, android.R.color.holo_orange_light) // Medium
                3 -> priorityIndicator.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, android.R.color.holo_green_light) // Low
                else -> priorityIndicator.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, android.R.color.darker_gray) // Default
            }

            itemView.setOnClickListener {
                onItemClicked(todoItem)
            }

            completedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onToggleCompleted(todoItem, isChecked)
            }
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
            return oldItem == newItem
        }
    }
}