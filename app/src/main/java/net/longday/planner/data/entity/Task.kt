package net.longday.planner.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    var title: String,
    var categoryId: String,
    var createdTime: Long = -1,
    var timeZone: String? = null,
    var content: String? = null,
    var dateTime: Long? = null,
    var completedTime: Long? = null,
    var deletedTime: Long? = null,
    var isDone: Boolean = false,
    var isDeleted: Boolean = false,
    var isScheduled: Boolean = false,
    var orderInCategory: Int = -1,
    var orderInTask: Int = -1, // not yet
    var isAllDay: Boolean = true,
    var parentTaskId: String? = null, // not yet
    var dueDate: String? = null, // not yet
    var startTime: String? = null, // not yet
    var endTime: String? = null, // not yet
    var icon: String? = null, // not yet
    var priority: String? = null, // not yet
    var isFocused: Boolean = false,
//    var isCanceled: Boolean = false,
//    var cancelReason: String? = null,
) : Parcelable