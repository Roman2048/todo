package net.longday.planner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    var title: String,
    val categoryId: String,
    var createdTime: Long = -1,
    var timeZone: String = "Europe/Moscow",
    var content: String? = "",
    var dateTime: Long?,
    var completedTime: Long? = -1,
    var dueDate: Long? = -1,
    var isDone: Boolean = false,
    var isDeleted: Boolean = false,
    var isScheduled: Boolean = false,
    var orderInCategory: Int? = -1,
) : Serializable