package net.longday.planner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tab")
data class Tab(
    @PrimaryKey val id: Int = 1,
    var selected: Int = 0,
) : Serializable
