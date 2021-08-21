package net.longday.planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import net.longday.planner.data.entity.Reminder
import net.longday.planner.data.repository.ReminderRepository
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(private val reminderRepository: ReminderRepository) :
    ViewModel() {

    val reminders = reminderRepository.reminders.filterNotNull().asLiveData()

    fun insert(reminder: Reminder) = CoroutineScope(Dispatchers.IO).launch {
        reminderRepository.insert(reminder)
    }

    fun update(reminder: Reminder) = CoroutineScope(Dispatchers.IO).launch {
        reminderRepository.update(reminder)
    }

    fun delete(reminder: Reminder) = CoroutineScope(Dispatchers.IO).launch {
        reminderRepository.delete(reminder)
    }
}