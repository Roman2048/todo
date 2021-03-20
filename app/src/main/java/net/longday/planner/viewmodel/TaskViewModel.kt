package net.longday.planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import net.longday.planner.data.entity.Task
import net.longday.planner.data.repository.TaskRepository
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    val tasks = taskRepository.tasks.filterNotNull().asLiveData()

    fun insert(task: Task) = CoroutineScope(Dispatchers.IO).launch {
        taskRepository.insert(task)
    }
}