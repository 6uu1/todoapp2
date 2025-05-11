package com.todo.mygo.todo.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todoItem: TodoItem)

    @Update
    suspend fun update(todoItem: TodoItem)

    @Delete
    suspend fun delete(todoItem: TodoItem)

    @Query("SELECT * FROM todo_items WHERE id = :id")
    suspend fun getTodoItemById(id: String): TodoItem?

    @Query("SELECT * FROM todo_items ORDER BY priority ASC, dueDate ASC, creationDate DESC")
    fun getAllTodoItems(): LiveData<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE isCompleted = :completed ORDER BY priority ASC, dueDate ASC, creationDate DESC")
    fun getTodoItemsByCompletionStatus(completed: Boolean): LiveData<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE groupId = :groupId ORDER BY priority ASC, dueDate ASC, creationDate DESC")
    fun getTodoItemsByGroupId(groupId: String): LiveData<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE parentId = :parentId ORDER BY priority ASC, dueDate ASC, creationDate DESC")
    fun getTodoItemsByParentId(parentId: String): LiveData<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE dueDate >= :startOfDay AND dueDate <= :endOfDay AND isCompleted = 0 ORDER BY priority ASC, dueDate ASC")
    fun getUncompletedTodoItemsForDateRange(startOfDay: Long, endOfDay: Long): LiveData<List<TodoItem>>
}