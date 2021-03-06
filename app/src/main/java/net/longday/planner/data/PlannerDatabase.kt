package net.longday.planner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.longday.planner.data.dao.TaskDao
import net.longday.planner.data.entity.Task

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}