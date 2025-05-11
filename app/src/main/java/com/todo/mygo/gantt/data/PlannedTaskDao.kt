package com.todo.mygo.gantt.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlannedTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedTasks(tasks: List<PlannedTask>)

    @Query("SELECT * FROM planned_task ORDER BY startTime ASC")
    fun getAllPlannedTasks(): LiveData<List<PlannedTask>>

    @Query("DELETE FROM planned_task")
    suspend fun clearAllPlannedTasks()
}