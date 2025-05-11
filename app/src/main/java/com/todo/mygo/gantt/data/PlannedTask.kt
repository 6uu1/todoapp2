package com.todo.mygo.gantt.data

import java.util.UUID
import androidx.room.Entity

@Entity
data class PlannedTask(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startTime: Long,
    val endTime: Long,
    val parentId: String? = null,
    val dependencyIds: List<String>? = null,
    val estimatedEffortHours: Float? = null,
    val actualEffortHours: Float? = null,
    val completionPercentage: Int = 0,
    val isMilestone: Boolean = false,
    val priority: Int? = null
    // 你可以根据需要添加其他必要的字段
)