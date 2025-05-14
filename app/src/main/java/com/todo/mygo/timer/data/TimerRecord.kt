package com.todo.mygo.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 正计时记录数据模型
 */
@Entity(tableName = "timer_records")
data class TimerRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String, // 计时标题
    val description: String? = null, // 描述（可选）
    val startTime: Long = System.currentTimeMillis(), // 开始时间
    var endTime: Long? = null, // 结束时间（为空表示计时仍在进行）
    var duration: Long = 0, // 持续时间（毫秒）
    var isPaused: Boolean = false, // 是否暂停
    var pauseStartTime: Long? = null, // 暂停开始时间
    var totalPausedTime: Long = 0, // 总暂停时间
    var associatedTaskId: String? = null // 关联的任务ID（可选）
)
