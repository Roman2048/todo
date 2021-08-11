package net.longday.planner.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.longday.planner.data.entity.Tab

@Dao
interface TabDao {

    @Query("SELECT * FROM tab WHERE id = 1")
    fun read(): Flow<Tab>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tab: Tab)

    @Delete
    suspend fun delete(tab: Tab)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(tab: Tab)
}