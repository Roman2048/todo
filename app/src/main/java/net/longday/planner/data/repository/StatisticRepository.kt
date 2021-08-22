package net.longday.planner.data.repository

import net.longday.planner.data.entity.Statistic
import net.longday.planner.retrofit.StatisticService
import javax.inject.Inject

class StatisticRepository @Inject constructor(private val statisticService: StatisticService) {
    suspend fun sendStatistic(statistic: Statistic) = statisticService.sendStatistic(statistic)
}