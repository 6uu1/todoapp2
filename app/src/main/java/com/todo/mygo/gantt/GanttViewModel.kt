package com.todo.mygo.gantt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.todo.mygo.gantt.data.PlannedTask // 新增 PlannedTask 的导入
import java.util.Calendar

class GanttViewModel(application: Application) : AndroidViewModel(application) {

    private val _plannedTasks = MutableLiveData<List<PlannedTask>>()
    val plannedTasks: LiveData<List<PlannedTask>> = _plannedTasks

    init {
        loadPlannedTasks()
    }

    private fun loadPlannedTasks() {
        val calendar = Calendar.getInstance()
        val tasks = mutableListOf<PlannedTask>()

        // 示例任务 1
        calendar.set(2024, Calendar.MAY, 10, 9, 0)
        val startTime1 = calendar.timeInMillis
        calendar.set(2024, Calendar.MAY, 12, 17, 0)
        val endTime1 = calendar.timeInMillis
        tasks.add(PlannedTask(name = "需求分析", startTime = startTime1, endTime = endTime1, completionPercentage = 20))

        // 示例任务 2
        calendar.set(2024, Calendar.MAY, 13, 9, 0)
        val startTime2 = calendar.timeInMillis
        calendar.set(2024, Calendar.MAY, 15, 12, 0)
        val endTime2 = calendar.timeInMillis
        tasks.add(PlannedTask(name = "UI 设计", startTime = startTime2, endTime = endTime2, completionPercentage = 60))

        // 示例任务 3
        calendar.set(2024, Calendar.MAY, 15, 13, 0)
        val startTime3 = calendar.timeInMillis
        calendar.set(2024, Calendar.MAY, 18, 17, 0)
        val endTime3 = calendar.timeInMillis
        tasks.add(PlannedTask(name = "API 开发", startTime = startTime3, endTime = endTime3, completionPercentage = 0))

        // 示例任务 4
        calendar.set(2024, Calendar.MAY, 19, 9, 0)
        val startTime4 = calendar.timeInMillis
        calendar.set(2024, Calendar.MAY, 22, 17, 0)
        val endTime4 = calendar.timeInMillis
        tasks.add(PlannedTask(name = "前端实现 - 列表页", startTime = startTime4, endTime = endTime4, isMilestone = true))

        // 示例任务 5
        calendar.set(2024, Calendar.MAY, 23, 9, 0)
        val startTime5 = calendar.timeInMillis
        calendar.set(2024, Calendar.MAY, 25, 17, 0)
        val endTime5 = calendar.timeInMillis
        tasks.add(PlannedTask(name = "前端实现 - 详情页", startTime = startTime5, endTime = endTime5, parentId = tasks[3].id, dependencyIds = listOf(tasks[2].id)))


        _plannedTasks.value = tasks
    }
}