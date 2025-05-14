package com.todo.mygo.timer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.mygo.R
import com.todo.mygo.timer.data.Reflection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 反思记录适配器
 */
class ReflectionAdapter(
    private val onEditClick: (Reflection) -> Unit,
    private val onDeleteClick: (Reflection) -> Unit
) : ListAdapter<Reflection, ReflectionAdapter.ReflectionViewHolder>(ReflectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReflectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reflection, parent, false)
        return ReflectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReflectionViewHolder, position: Int) {
        val reflection = getItem(position)
        holder.bind(reflection)
    }

    inner class ReflectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentTextView: TextView = itemView.findViewById(R.id.reflectionContent)
        private val timeTextView: TextView = itemView.findViewById(R.id.reflectionTime)
        private val editButton: ImageButton = itemView.findViewById(R.id.btnEditReflection)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btnDeleteReflection)
        
        fun bind(reflection: Reflection) {
            contentTextView.text = reflection.content
            timeTextView.text = formatDate(reflection.creationTime)
            
            editButton.setOnClickListener {
                onEditClick(reflection)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(reflection)
            }
            
            // 点击整个条目也可以编辑
            itemView.setOnClickListener {
                onEditClick(reflection)
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return dateFormat.format(Date(timestamp))
        }
    }
}

/**
 * 反思记录差异比较回调
 */
class ReflectionDiffCallback : DiffUtil.ItemCallback<Reflection>() {
    override fun areItemsTheSame(oldItem: Reflection, newItem: Reflection): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Reflection, newItem: Reflection): Boolean {
        return oldItem == newItem
    }
}
