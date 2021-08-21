package net.longday.planner.data.repository

import net.longday.planner.data.dao.ReminderDao
import net.longday.planner.data.entity.Reminder
import javax.inject.Inject

class ReminderRepository @Inject constructor(private val reminderDao: ReminderDao) {

    val reminders = reminderDao.getAll()

    suspend fun insert(reminder: Reminder) = reminderDao.insert(reminder)

    suspend fun update(reminder: Reminder) = reminderDao.update(reminder)

    suspend fun delete(reminder: Reminder) = reminderDao.delete(reminder)
}