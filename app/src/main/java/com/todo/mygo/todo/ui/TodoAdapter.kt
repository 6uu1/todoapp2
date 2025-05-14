package com.todo.mygo.todo.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
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
    private val onToggleCompleted: (TodoItem, Boolean) -> Unit,
    private val onToggleStarred: ((TodoItem, Boolean) -> Unit)? = null
) : ListAdapter<TodoItem, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo_improved, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClicked, onToggleCompleted, onToggleStarred)
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvTodoTitle)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tvTodoCategory)
        private val completedCheckBox: CheckBox = itemView.findViewById(R.id.cbTodoCompleted)
        private val starButton: ImageButton = itemView.findViewById(R.id.btnStar)

        fun bind(
            todoItem: TodoItem,
            onItemClicked: (TodoItem) -> Unit,
            onToggleCompleted: (TodoItem, Boolean) -> Unit,
            onToggleStarred: ((TodoItem, Boolean) -> Unit)?
        ) {
            titleTextView.text = todoItem.title
            completedCheckBox.isChecked = todoItem.isCompleted

            // 设置类别文本（使用描述或优先级）
            val categoryText = when {
                !todoItem.description.isNullOrEmpty() -> todoItem.description
                else -> getCategoryFromPriority(todoItem.priority)
            }
            categoryTextView.text = categoryText

            // 设置完成状态的样式
            if (todoItem.isCompleted) {
                titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                categoryTextView.paintFlags = categoryTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                titleTextView.paintFlags = titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                categoryTextView.paintFlags = categoryTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // 设置星标状态（这里假设TodoItem有isStarred属性，如果没有可以添加）
            val isStarred = todoItem.priority == 1 // 临时使用优先级1作为星标状态
            starButton.setImageResource(
                if (isStarred) R.drawable.ic_star_filled_24dp else R.drawable.ic_star_border_24dp
            )

            // 设置点击事件
            itemView.setOnClickListener {
                onItemClicked(todoItem)
            }

            completedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onToggleCompleted(todoItem, isChecked)
            }

            starButton.setOnClickListener {
                onToggleStarred?.invoke(todoItem, !isStarred)
            }
        }

        private fun getCategoryFromPriority(priority: Int): String {
            return when (priority) {
                1 -> "重要"
                2 -> "一般"
                3 -> "次要"
                else -> "任务"
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