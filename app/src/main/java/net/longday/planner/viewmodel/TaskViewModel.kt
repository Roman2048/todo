package net.longday.planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun insert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        taskRepository.insert(task)
    }

    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        taskRepository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        taskRepository.delete(task)
    }
}