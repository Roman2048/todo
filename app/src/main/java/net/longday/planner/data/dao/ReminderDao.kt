package net.longday.planner.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.longday.planner.data.entity.Reminder

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders")
    fun getAll(): Flow<List<Reminder>>

    @Insert
    suspend fun insert(reminder: Reminder)

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)
}