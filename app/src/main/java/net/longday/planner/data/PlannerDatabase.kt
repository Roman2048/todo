package net.longday.planner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import net.longday.planner.data.dao.CategoryDao
import net.longday.planner.data.dao.TabDao
import net.longday.planner.data.dao.TaskDao
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Tab
import net.longday.planner.data.entity.Task

@Database(entities = [Task::class, Category::class, Tab::class], version = 1, exportSchema = false)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tabDao(): TabDao
}