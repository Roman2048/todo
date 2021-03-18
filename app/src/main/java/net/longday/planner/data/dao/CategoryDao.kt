package net.longday.planner.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.longday.planner.data.entity.Category

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories")
    fun getAll(): Flow<List<Category>>

    @Insert
    suspend fun insert(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Update
    suspend fun update(category: Category)
}