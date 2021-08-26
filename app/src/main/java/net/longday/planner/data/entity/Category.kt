package net.longday.planner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val title: String,
    var position: Int = -1,
    var icon: String? = null,
) : Serializable
