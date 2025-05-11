package com.todo.mygo.calendar.data

import androidx.lifecycle.LiveData
import com.todo.mygo.gantt.data.PlannedTask
import com.todo.mygo.gantt.data.PlannedTaskDao
import com.todo.mygo.todo.data.TodoItem // Added import
import com.todo.mygo.todo.data.TodoDao   // Added import

class CalendarRepository(
    private val eventDao: EventDao,
    private val plannedTaskDao: PlannedTaskDao,
    private val todoDao: TodoDao // Added TodoDao
) {

    val allEvents: LiveData<List<Event>> = eventDao.getAllEvents()

    // Event methods
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

    // PlannedTask methods
    fun getAllPlannedTasks(): LiveData<List<PlannedTask>> {
        return plannedTaskDao.getAllPlannedTasks()
    }

    suspend fun insertPlannedTasks(tasks: List<PlannedTask>) {
        plannedTaskDao.insertPlannedTasks(tasks)
    }

    suspend fun clearAllPlannedTasks() {
        plannedTaskDao.clearAllPlannedTasks()
    }

    // TodoItem methods
    val allTodoItems: LiveData<List<TodoItem>> = todoDao.getAllTodoItems()

    suspend fun insertTodoItem(todoItem: TodoItem) {
        todoDao.insert(todoItem)
    }

    suspend fun updateTodoItem(todoItem: TodoItem) {
        todoDao.update(todoItem)
    }

    suspend fun deleteTodoItem(todoItem: TodoItem) {
        todoDao.delete(todoItem)
    }

    suspend fun getTodoItemById(id: String): TodoItem? {
        return todoDao.getTodoItemById(id)
    }

    fun getTodoItemsByCompletionStatus(completed: Boolean): LiveData<List<TodoItem>> {
        return todoDao.getTodoItemsByCompletionStatus(completed)
    }

    fun getTodoItemsByGroupId(groupId: String): LiveData<List<TodoItem>> {
        return todoDao.getTodoItemsByGroupId(groupId)
    }

    fun getTodoItemsByParentId(parentId: String): LiveData<List<TodoItem>> {
        return todoDao.getTodoItemsByParentId(parentId)
    }

    fun getUncompletedTodoItemsForDateRange(startOfDay: Long, endOfDay: Long): LiveData<List<TodoItem>> {
        return todoDao.getUncompletedTodoItemsForDateRange(startOfDay, endOfDay)
    }
}