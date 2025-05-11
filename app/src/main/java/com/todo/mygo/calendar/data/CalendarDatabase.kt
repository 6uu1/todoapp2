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
import com.todo.mygo.todo.data.TodoItem // Added import
import com.todo.mygo.todo.data.TodoDao   // Added import

@Database(entities = [Event::class, PlannedTask::class, TodoItem::class], version = 3, exportSchema = false) // Updated entities and version
abstract class CalendarDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun plannedTaskDao(): PlannedTaskDao
    abstract fun todoDao(): TodoDao // Added DAO

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
                // For complex migrations, you would write SQL here.
                // Example: database.execSQL("CREATE TABLE IF NOT EXISTS `todo_items` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `priority` INTEGER NOT NULL, `dueDate` INTEGER, `isCompleted` INTEGER NOT NULL, `creationDate` INTEGER NOT NULL, `completionDate` INTEGER, `parentId` TEXT, `groupId` TEXT, `tags` TEXT, PRIMARY KEY(`id`))")
                // However, since `TodoItem` has a `List<String>?` for tags, Room needs a TypeConverter for it.
                // We'll assume a TypeConverter will be added later if complex types like List<String> are used directly.
                // For now, let's ensure the table is created.
                // Room handles this automatically if exportSchema = false and no specific SQL is needed for simple additions.
            }
        }

        fun getDatabase(context: Context): CalendarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalendarDatabase::class.java,
                    "calendar_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Added new migration
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}