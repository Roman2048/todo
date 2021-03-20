package net.longday.planner.data.repository

import net.longday.planner.data.dao.CategoryDao
import net.longday.planner.data.entity.Category
import javax.inject.Inject

class CategoryRepository @Inject constructor(private val categoryDao: CategoryDao) {

    val categories = categoryDao.getAll()

    suspend fun insert(category: Category) = categoryDao.insert(category)

    suspend fun update(category: Category) = categoryDao.update(category)

    suspend fun delete(category: Category) = categoryDao.delete(category)
}