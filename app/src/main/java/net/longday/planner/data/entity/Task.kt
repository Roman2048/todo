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
    var scheduledType: String? = null, // day, week, month, year
    var scheduledTime: Long? = null, // time
    var scheduledEvery: Long? = null, // every 1..365 days, every 1..11 month, every 1..99 years
    var scheduledDayStartWith: Long? = null, // day to start scheduled task
    var scheduledDaysOfWeek: String? = null, // days in week when scheduled task is active (MON, FRI)
    var scheduledMonthByDay: String? = null, // Monthly, day in month
    var scheduledMonthStartWithMonth: String? = null, // Monthly, month to start
    var scheduledMonthByNumberInWeek: String? = null, // Monthly, week number in month (1..4, LAST)
    var scheduledMonthByNumberInWeekDayTitle: String? = null, // Monthly, day in week (FRI)
    var scheduledYearByDay: String? = null, // Yearly, day in year
    var orderInCategory: Int = -1,
    var orderInTask: Int = -1, // not yet
    var isAllDay: Boolean = true,
    var parentTaskId: String? = null, // not yet
    var dueDate: Long? = null, // not yet
    var startTime: Long? = null, // not yet
    var endTime: Long? = null, // not yet
    var icon: String? = null, // not yet
    var priority: String? = null, // not yet
    var isFocused: Boolean = false,
    var isCanceled: Boolean = false,
    var cancelReason: String? = null,
    var cancelTime: Long? = null,
) : Parcelable