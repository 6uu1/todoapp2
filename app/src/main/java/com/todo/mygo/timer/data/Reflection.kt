package com.todo.mygo.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 反思记录数据模型
 */
@Entity(tableName = "reflections")
data class Reflection(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val content: String, // 反思内容
    val creationTime: Long = System.currentTimeMillis(), // 创建时间
    var lastModifiedTime: Long = System.currentTimeMillis(), // 最后修改时间
    val associatedDate: Long = System.currentTimeMillis(), // 关联日期（默认为创建日期）
    val tags: List<String>? = null // 标签（可选）
)
