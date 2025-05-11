package com.todo.mygo.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val priority: Int = 2, // 1-高, 2-中, 3-低
    val dueDate: Long? = null,
    var isCompleted: Boolean = false,
    val creationDate: Long = System.currentTimeMillis(),
    var completionDate: Long? = null,
    val parentId: String? = null,
    val groupId: String? = null,
    val tags: List<String>? = null
)