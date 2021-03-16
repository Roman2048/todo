package net.longday.planner.data.repository

import kotlinx.coroutines.flow.filterNotNull
import net.longday.planner.data.dao.TaskDao
import net.longday.planner.data.entity.Task
import javax.inject.Inject

class TaskRepository @Inject constructor(private val taskDao: TaskDao) {

    val tasks = taskDao.getAll().filterNotNull()

    suspend fun insert(task: Task) = taskDao.insert(task)

}