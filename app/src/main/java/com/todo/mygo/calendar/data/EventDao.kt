package com.todo.mygo.calendar.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM calendar_events WHERE id = :eventId")
    fun getEventById(eventId: Long): LiveData<Event?>

    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    fun getAllEvents(): LiveData<List<Event>>

    // Query for events within a specific date range (e.g., for a day, week, or month view)
    @Query("SELECT * FROM calendar_events WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime ASC")
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): LiveData<List<Event>>

    @Query("SELECT * FROM calendar_events WHERE startTime >= :startOfWeek AND startTime < :endOfWeek ORDER BY startTime ASC")
    fun getEventsForWeek(startOfWeek: Long, endOfWeek: Long): LiveData<List<Event>>

    @Query("SELECT * FROM calendar_events WHERE startTime >= :startOfMonth AND startTime < :endOfMonth ORDER BY startTime ASC")
    fun getEventsForMonth(startOfMonth: Long, endOfMonth: Long): LiveData<List<Event>>

    // Search query
    @Query("SELECT * FROM calendar_events WHERE title LIKE :searchQuery OR description LIKE :searchQuery OR category LIKE :searchQuery OR tags LIKE :searchQuery ORDER BY startTime ASC")
    fun searchEvents(searchQuery: String): LiveData<List<Event>>

    // Query by category
    @Query("SELECT * FROM calendar_events WHERE category = :categoryName ORDER BY startTime ASC")
    fun getEventsByCategory(categoryName: String): LiveData<List<Event>>

    // Query by tag (assuming tags are stored as comma-separated string, using LIKE for partial match)
    @Query("SELECT * FROM calendar_events WHERE tags LIKE '%' || :tagName || '%' ORDER BY startTime ASC")
    fun getEventsByTag(tagName: String): LiveData<List<Event>>
}