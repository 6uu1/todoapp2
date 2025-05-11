package com.todo.mygo.todo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.todo.mygo.calendar.data.CalendarDatabase
import com.todo.mygo.calendar.data.CalendarRepository
import com.todo.mygo.todo.data.TodoItem
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository
    val allTodoItems: LiveData<List<TodoItem>>

    // For handling navigation to an edit screen or showing a dialog
    private val _navigateToEditTodo = MutableLiveData<TodoItem?>()
    val navigateToEditTodo: LiveData<TodoItem?>
        get() = _navigateToEditTodo

    init {
        val calendarDb = CalendarDatabase.getDatabase(application)
        val todoDao = calendarDb.todoDao()
        val eventDao = calendarDb.eventDao() // Required by CalendarRepository constructor
        val plannedTaskDao = calendarDb.plannedTaskDao() // Required by CalendarRepository constructor
        repository = CalendarRepository(eventDao, plannedTaskDao, todoDao)
        allTodoItems = repository.allTodoItems
    }

    fun insert(todoItem: TodoItem) = viewModelScope.launch {
        repository.insertTodoItem(todoItem)
    }

    fun update(todoItem: TodoItem) = viewModelScope.launch {
        repository.updateTodoItem(todoItem)
    }

    fun delete(todoItem: TodoItem) = viewModelScope.launch {
        repository.deleteTodoItem(todoItem)
    }

    fun toggleCompleted(todoItem: TodoItem, isCompleted: Boolean) = viewModelScope.launch {
        val updatedItem = todoItem.copy(
            isCompleted = isCompleted,
            completionDate = if (isCompleted) System.currentTimeMillis() else null
        )
        repository.updateTodoItem(updatedItem)
    }

    fun onTodoItemClicked(todoItem: TodoItem) {
        _navigateToEditTodo.value = todoItem
    }

    fun onEditTodoNavigated() {
        _navigateToEditTodo.value = null
    }

    // Optional: Methods to get filtered lists
    fun getTodoItemsByCompletionStatus(completed: Boolean): LiveData<List<TodoItem>> {
        return repository.getTodoItemsByCompletionStatus(completed)
    }

    fun getTodoItemsByGroupId(groupId: String): LiveData<List<TodoItem>> {
        return repository.getTodoItemsByGroupId(groupId)
    }
}