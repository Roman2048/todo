package net.longday.planner.data.entity

import androidx.room.PrimaryKey
import java.util.*

data class Statistic(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val categories: Int,
    val tasks: Int,
    val reminders: Int,
)
