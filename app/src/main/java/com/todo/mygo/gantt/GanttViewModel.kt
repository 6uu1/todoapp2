package com.todo.mygo.gantt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.todo.mygo.calendar.data.CalendarDatabase
import com.todo.mygo.calendar.data.CalendarRepository
import com.todo.mygo.calendar.data.Event
import kotlinx.coroutines.launch

class GanttViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository
    val allEvents: LiveData<List<Event>>

    init {
        val eventDao = CalendarDatabase.getDatabase(application).eventDao()
        repository = CalendarRepository(eventDao)
        allEvents = repository.allEvents
    }

    // Potentially add methods here to filter or process events specifically for Gantt view
    // For example, to handle parent/child relationships or sort by start time.
}