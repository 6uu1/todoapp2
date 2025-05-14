package com.todo.mygo.calendar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.todo.mygo.gantt.data.PlannedTask
import com.todo.mygo.gantt.data.PlannedTaskDao
import com.todo.mygo.gantt.data.Converters // Added import for Gantt Converters
import com.todo.mygo.todo.data.TodoItem // Added import
import com.todo.mygo.todo.data.TodoDao   // Added import
import com.todo.mygo.timer.data.PomodoroSession // Added import for Timer
import com.todo.mygo.timer.data.TimerRecord // Added import for Timer
import com.todo.mygo.timer.data.Reflection // Added import for Timer
import com.todo.mygo.timer.data.TimerDao // Added import for Timer

@Database(entities = [Event::class, PlannedTask::class, TodoItem::class, PomodoroSession::class, TimerRecord::class, Reflection::class], version = 4, exportSchema = false) // Updated entities and version
@TypeConverters(Converters::class) // Added Converters
abstract class CalendarDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun plannedTaskDao(): PlannedTaskDao
    abstract fun todoDao(): TodoDao // Added DAO
    abstract fun timerDao(): TimerDao // Added Timer DAO

    companion object {
        @Volatile
        private var INSTANCE: CalendarDatabase? = null

        // Migration from version 1 to 2: Add PlannedTask table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Room will automatically create the new table for PlannedTask
                // because it's added to the entities list and version is incremented.
            }
        }

        // Migration from version 2 to 3: Add TodoItem table
        val MIGRATION_2_3 = object : Migration(2, 3) { // Added migration
            override fun migrate(database: SupportSQLiteDatabase) {
                // Room will automatically create the new table for TodoItem
                // because it's added to the entities list and version is incremented.
            }
        }

        // Migration from version 3 to 4: Add Timer related tables
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Room will automatically create the new tables for PomodoroSession, TimerRecord, and Reflection
                // because they're added to the entities list and version is incremented.
            }
        }

        fun getDatabase(context: Context): CalendarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalendarDatabase::class.java,
                    "calendar_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // Added new migration
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}