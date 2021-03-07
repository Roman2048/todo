package net.longday.planner.adapter

import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import java.util.*

class FakeDataset {

 companion object {
     val tasks = mutableListOf(
         Task(UUID.randomUUID().toString(), "task title", "0"),
         Task(UUID.randomUUID().toString(), "task title", "0"),
         Task(UUID.randomUUID().toString(), "task title", "0"),
         Task(UUID.randomUUID().toString(), "task title", "0"),
         Task(UUID.randomUUID().toString(), "task title", "0"),
         Task(UUID.randomUUID().toString(), "task title", "0"),
         Task(UUID.randomUUID().toString(), "task title", "0")
     )

     val categories = mutableListOf(
         Category(UUID.randomUUID().toString(), "cat 1", 0),
         Category(UUID.randomUUID().toString(), "cat 2", 0),
         Category(UUID.randomUUID().toString(), "cat 3", 0)
     )

 }

}