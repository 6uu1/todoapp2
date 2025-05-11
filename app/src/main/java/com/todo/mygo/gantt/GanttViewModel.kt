package com.todo.mygo.gantt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
// import androidx.lifecycle.MutableLiveData // No longer needed for _plannedTasks
import com.todo.mygo.calendar.data.CalendarDatabase
import com.todo.mygo.calendar.data.CalendarRepository
import com.todo.mygo.gantt.data.PlannedTask
// import java.util.Calendar // No longer needed for example data

class GanttViewModel(application: Application) : AndroidViewModel(application) {

    private val calendarRepository: CalendarRepository
    val plannedTasks: LiveData<List<PlannedTask>>

    init {
        val database = CalendarDatabase.getDatabase(application)
        calendarRepository = CalendarRepository(database.eventDao(), database.plannedTaskDao())
        plannedTasks = calendarRepository.getAllPlannedTasks()
    }

    // The loadPlannedTasks method and its example data are no longer needed,
    // as tasks are now loaded directly from the repository.
}