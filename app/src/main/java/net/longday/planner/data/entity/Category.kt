package net.longday.planner.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val title: String,
    var position: Int = -1,
    var icon: String? = null,
) : Parcelable
