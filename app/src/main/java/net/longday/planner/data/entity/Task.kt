package net.longday.planner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.Instant

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    var title: String,
    val categoryId: String,
    var dateTime: String,
) : Serializable