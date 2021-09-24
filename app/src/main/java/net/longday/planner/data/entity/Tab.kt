package net.longday.planner.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tab")
data class Tab(
    @PrimaryKey val id: Int = 1,
    var selected: Int = 0,
) : Parcelable
