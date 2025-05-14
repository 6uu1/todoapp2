package com.todo.mygo.timer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 番茄钟会话数据模型
 */
@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    val workDuration: Int = 25, // 工作时长（分钟）
    val shortBreakDuration: Int = 5, // 短休息时长（分钟）
    val longBreakDuration: Int = 15, // 长休息时长（分钟）
    val longBreakInterval: Int = 4, // 长休息间隔（完成几个番茄后进行长休息）
    var completedPomodoros: Int = 0, // 已完成的番茄数量
    var currentStatus: PomodoroStatus = PomodoroStatus.IDLE, // 当前状态
    var associatedTaskId: String? = null // 关联的任务ID（可选）
)

/**
 * 番茄钟状态枚举
 */
enum class PomodoroStatus {
    IDLE, // 空闲
    WORKING, // 工作中
    SHORT_BREAK, // 短休息
    LONG_BREAK, // 长休息
    PAUSED // 暂停
}
