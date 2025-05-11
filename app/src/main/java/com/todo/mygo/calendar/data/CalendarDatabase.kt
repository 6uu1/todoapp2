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

@Database(entities = [Event::class, PlannedTask::class], version = 2, exportSchema = false)
abstract class CalendarDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun plannedTaskDao(): PlannedTaskDao

    companion object {
        @Volatile
        private var INSTANCE: CalendarDatabase? = null

        // Migration from version 1 to 2: Add PlannedTask table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Room will automatically create the new table for PlannedTask
                // because it's added to the entities list and version is incremented.
                // If you had schema changes for existing tables, you'd write SQL here.
            }
        }

        fun getDatabase(context: Context): CalendarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalendarDatabase::class.java,
                    "calendar_database"
                )
                .addMigrations(MIGRATION_1_2) // Add our migration strategy
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}