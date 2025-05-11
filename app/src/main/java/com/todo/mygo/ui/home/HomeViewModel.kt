package com.todo.mygo.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.todo.mygo.calendar.data.CalendarDatabase
import com.todo.mygo.calendar.data.CalendarRepository
import com.todo.mygo.todo.data.TodoItem
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository
    val todaysTodoItemsText: LiveData<String>

    init {
        val calendarDb = CalendarDatabase.getDatabase(application)
        val todoDao = calendarDb.todoDao()
        val eventDao = calendarDb.eventDao()
        val plannedTaskDao = calendarDb.plannedTaskDao()
        repository = CalendarRepository(eventDao, plannedTaskDao, todoDao)

        val calendar = Calendar.getInstance()
        // Set to start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // Set to end of today
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        val todaysItemsLiveData: LiveData<List<TodoItem>> = repository.getUncompletedTodoItemsForDateRange(startOfDay, endOfDay)

        todaysTodoItemsText = todaysItemsLiveData.map { items ->
            if (items.isNullOrEmpty()) {
                "今天没有待办事项。"
            } else {
                "今天有 ${items.size} 个待办事项：\n" + items.joinToString("\n") { "- ${it.title}" }
            }
        }
    }

    // Keep the original 'text' LiveData for now, or decide if it should be replaced by todaysTodoItemsText
    // For this task, we will replace it as per the objective to show today's todos.
    val text: LiveData<String> = todaysTodoItemsText
}