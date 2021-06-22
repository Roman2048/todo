package net.longday.planner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import net.longday.planner.data.dao.CategoryDao
import net.longday.planner.data.dao.TaskDao
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task

@Database(entities = [Task::class, Category::class], version = 1, exportSchema = true)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
}