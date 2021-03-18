package net.longday.planner.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.longday.planner.data.entity.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun getAll(): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Update
    suspend fun update(task: Task)
}