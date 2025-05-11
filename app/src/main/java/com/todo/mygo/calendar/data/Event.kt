package com.todo.mygo.calendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "calendar_events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var title: String,
    var description: String? = null,
    var startTime: Long, // Store as Long (timestamp) for easier Room handling
    var endTime: Long,   // Store as Long (timestamp)
    var priority: Int = 0, // 0: Low, 1: Medium, 2: High (can define constants or enum later)
    var category: String? = null, // Simplified: store category name directly
    var tags: String? = null,     // Simplified: comma-separated string of tags
    var isAllDay: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var parentId: Long? = null // ID of the parent event, null if it's a top-level event
)