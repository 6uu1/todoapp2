package com.todo.mygo.ai_planner.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.mygo.databinding.ItemPlannedTaskBinding // Generated from item_planned_task.xml
import com.todo.mygo.gantt.data.PlannedTask
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlannedTaskAdapter : ListAdapter<PlannedTask, PlannedTaskAdapter.PlannedTaskViewHolder>(PlannedTaskDiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlannedTaskViewHolder {
        val binding = ItemPlannedTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlannedTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlannedTaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlannedTaskViewHolder(private val binding: ItemPlannedTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: PlannedTask) {
            binding.textViewTaskName.text = task.name
            val startTimeStr = dateFormat.format(Date(task.startTime))
            val endTimeStr = dateFormat.format(Date(task.endTime))
            binding.textViewTaskTime.text = "开始: $startTimeStr - 结束: $endTimeStr"
        }
    }

    class PlannedTaskDiffCallback : DiffUtil.ItemCallback<PlannedTask>() {
        override fun areItemsTheSame(oldItem: PlannedTask, newItem: PlannedTask): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlannedTask, newItem: PlannedTask): Boolean {
            return oldItem == newItem
        }
    }
}