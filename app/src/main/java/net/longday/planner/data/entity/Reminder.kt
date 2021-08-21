package net.longday.planner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String? = null,
    val workerId: String? = null,
    val time: Long? = null,
) : Serializable