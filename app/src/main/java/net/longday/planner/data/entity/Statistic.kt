package net.longday.planner.data.entity

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Statistic(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val categories: Int,
    val tasks: Int,
    val reminders: Int,
) : Parcelable
