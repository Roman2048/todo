package net.longday.planner.retrofit

import net.longday.planner.data.entity.Statistic
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface StatisticService {
    @POST("/statistic")
    fun sendStatistic(@Body statistic: Statistic): Call<Void>
}