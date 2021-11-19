package net.longday.planner.data.entity

import java.text.SimpleDateFormat
import java.util.*

data class ExportedTask(
    var title: String?,
    var category: String?,
    var created: String?,
    var content: String?,
    var done: String?,
)

fun Task.export(categories: List<Category>): ExportedTask {
    return ExportedTask(
        this.title,
        categories.first { it.id == this.categoryId }.title,
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(this.createdTime),
        if (this.content.isNullOrBlank()) null else this.content,
        if (this.isDone) "yes" else "no",
    )
}