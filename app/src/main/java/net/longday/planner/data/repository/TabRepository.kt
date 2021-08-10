//package net.longday.planner.data.repository
//
//import net.longday.planner.data.dao.TabDao
//import net.longday.planner.data.entity.Tab
//import javax.inject.Inject
//
//class TabRepository @Inject constructor(private val tabDao: TabDao) {
//
//    val tab = tabDao.read()
//
//    suspend fun insert(tab: Tab) = tabDao.insert(tab)
//
//    suspend fun update(tab: Tab) = tabDao.update(tab)
//
//    suspend fun delete(tab: Tab) = tabDao.delete(tab)
//}