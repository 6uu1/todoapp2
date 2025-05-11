package com.todo.mygo.calendar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.todo.mygo.calendar.data.CalendarDatabase
import com.todo.mygo.calendar.data.CalendarRepository
import com.todo.mygo.calendar.data.Event
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository
    val allEvents: LiveData<List<Event>>

    // LiveData for displaying events based on selected view (day, week, month)
    private val _selectedDateEvents = MutableLiveData<List<Event>>()
    val selectedDateEvents: LiveData<List<Event>> = _selectedDateEvents

    // LiveData for the currently selected date
    private val _selectedDate = MutableLiveData<Date>(Date()) // Default to today
    val selectedDate: LiveData<Date> = _selectedDate

    init {
        val eventDao = CalendarDatabase.getDatabase(application).eventDao()
        repository = CalendarRepository(eventDao)
        allEvents = repository.allEvents
        // Observe selectedDate changes to update events
        _selectedDate.observeForever { date ->
            loadEventsForDate(date)
        }
    }

    fun insertEvent(event: Event) = viewModelScope.launch {
        repository.insertEvent(event)
    }

    fun updateEvent(event: Event) = viewModelScope.launch {
        repository.updateEvent(event)
    }

    fun deleteEvent(event: Event) = viewModelScope.launch {
        repository.deleteEvent(event)
    }

    fun getEventById(eventId: Long): LiveData<Event?> {
        return repository.getEventById(eventId)
    }

    fun searchEvents(query: String): LiveData<List<Event>> {
        return repository.searchEvents(query)
    }

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
    }

    // Example: Load events for the selected day
    private fun loadEventsForDate(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        // This is a simplified example; you'd likely have different logic
        // for week/month views and potentially observe repository methods directly.
        // For now, let's assume we are loading daily events.
        // You might want to switch the source LiveData based on a view type (day/week/month).
        viewModelScope.launch {
            // This is not ideal as it re-queries.
            // A better approach would be to transform allEvents or use specific DAO queries.
            // For simplicity now, we'll filter from allEvents.
            // In a real app, you'd use repository.getEventsForDay(startOfDay, endOfDay)
            // and manage the LiveData subscription appropriately.
            // For now, let's just post all events to demonstrate connection.
            // TODO: Implement proper day/week/month loading logic
             _selectedDateEvents.postValue(allEvents.value?.filter {
                 it.startTime >= startOfDay && it.startTime < endOfDay
             } ?: emptyList())
        }
    }

    // Placeholder for loading events for the current week
    fun loadEventsForWeek(dateInWeek: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = dateInWeek
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        // Normalize to start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val endOfWeek = calendar.timeInMillis

        // TODO: Replace with actual repository call and LiveData observation
        // repository.getEventsForWeek(startOfWeek, endOfWeek).observe...
        // For now, filtering allEvents as a placeholder
        viewModelScope.launch {
            _selectedDateEvents.postValue(allEvents.value?.filter {
                it.startTime >= startOfWeek && it.startTime < endOfWeek
            } ?: emptyList())
        }
    }

    // Placeholder for loading events for the current month
    fun loadEventsForMonth(dateInMonth: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = dateInMonth
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        // Normalize to start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis

        // TODO: Replace with actual repository call and LiveData observation
        // repository.getEventsForMonth(startOfMonth, endOfMonth).observe...
        // For now, filtering allEvents as a placeholder
        viewModelScope.launch {
            _selectedDateEvents.postValue(allEvents.value?.filter {
                it.startTime >= startOfMonth && it.startTime < endOfMonth
            } ?: emptyList())
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove observers to prevent memory leaks
        _selectedDate.removeObserver { } // Simplified removal
    }
}