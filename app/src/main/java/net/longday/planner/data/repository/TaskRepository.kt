package net.longday.planner.data.repository

import kotlinx.coroutines.flow.filterNotNull
import net.longday.planner.data.dao.TaskDao

class TaskRepository(private val taskDao: TaskDao) {

    val tasks = taskDao.getAll().filterNotNull()
}