package net.longday.planner.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String? = null,
    val workerId: String? = null,
    val time: Long? = null,
) : Parcelable