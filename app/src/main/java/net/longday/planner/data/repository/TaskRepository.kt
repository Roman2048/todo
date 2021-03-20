package net.longday.planner.data.repository

import net.longday.planner.data.dao.TaskDao
import net.longday.planner.data.entity.Task
import javax.inject.Inject

class TaskRepository @Inject constructor(private val taskDao: TaskDao) {

    val tasks = taskDao.getAll()

    suspend fun insert(task: Task) = taskDao.insert(task)

    suspend fun update(task: Task) = taskDao.update(task)

    suspend fun delete(task: Task) = taskDao.delete(task)
}