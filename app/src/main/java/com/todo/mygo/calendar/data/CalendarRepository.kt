package com.todo.mygo.calendar.data

import androidx.lifecycle.LiveData

class CalendarRepository(private val eventDao: EventDao) {

    val allEvents: LiveData<List<Event>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: Event): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    fun getEventById(eventId: Long): LiveData<Event?> {
        return eventDao.getEventById(eventId)
    }

    fun getEventsForDay(startOfDay: Long, endOfDay: Long): LiveData<List<Event>> {
        return eventDao.getEventsForDay(startOfDay, endOfDay)
    }

    fun getEventsForWeek(startOfWeek: Long, endOfWeek: Long): LiveData<List<Event>> {
        return eventDao.getEventsForWeek(startOfWeek, endOfWeek)
    }

    fun getEventsForMonth(startOfMonth: Long, endOfMonth: Long): LiveData<List<Event>> {
        return eventDao.getEventsForMonth(startOfMonth, endOfMonth)
    }

    fun searchEvents(searchQuery: String): LiveData<List<Event>> {
        return eventDao.searchEvents("%${searchQuery}%") // Add wildcards for LIKE query
    }

    fun getEventsByCategory(categoryName: String): LiveData<List<Event>> {
        return eventDao.getEventsByCategory(categoryName)
    }

    fun getEventsByTag(tagName: String): LiveData<List<Event>> {
        return eventDao.getEventsByTag(tagName) // DAO handles wildcard for tag search
    }
}