package net.longday.planner.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock.elapsedRealtime
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import net.longday.planner.R
import net.longday.planner.ReminderActivity
import net.longday.planner.adapter.ViewPagerAdapter
import net.longday.planner.data.entity.Category
import net.longday.planner.data.entity.Task
import net.longday.planner.viewmodel.CategoryViewModel
import net.longday.planner.viewmodel.TaskViewModel
import net.longday.planner.work.NotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Основной рабочий экран приложения.
 * Сверху отображается список категорий в виде вкладок, и кнопка для перехода на страницу
 * для их редактирования.
 * Основное содержимое - список задач для выбранной влкадки.
 */
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val taskViewModel: TaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var categoryId = ""
        val viewPager: ViewPager2 = view.findViewById(R.id.category_view_pager)
        val pagerAdapter = ViewPagerAdapter(this, listOf())
        viewPager.adapter = pagerAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        var categories: List<Category> = listOf()
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categories = it
            pagerAdapter.categories = it
            pagerAdapter.notifyDataSetChanged()
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = it[position].title

            }.attach()
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                categoryId = categories[tab!!.position].id
                // cast fragment to your fragment class and do what you want
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            BottomSheetDialog(requireContext(), R.style.BottomSheetStyle).apply {
                it.showKeyboard()
//                this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                setContentView(layoutInflater.inflate(R.layout.bottom_sheet, null))
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                this.findViewById<TextInputLayout>(R.id.bottom_sheet_edit_text)?.requestFocus()
                show()
//                it.showKeyboard()
                var dayTime: Long? = null
                this.findViewById<Button>(R.id.new_task_save_button)?.setOnClickListener {
                    if (categoryId != "") {
                        taskViewModel.insert(
                            Task(
                                id = UUID.randomUUID().toString(),
                                title = this.findViewById<TextInputLayout>(R.id.bottom_sheet_edit_text)?.editText?.text.toString(),
                                categoryId = categoryId,
//                                dateTime = System.currentTimeMillis().toString(),
                                dateTime = dayTime,
                            )
                        )
                    }
                    this.findViewById<TextInputLayout>(R.id.bottom_sheet_edit_text)?.editText?.setText("")
                    dayTime = null
                    it.hideKeyboard()
                    this.hide()
//                    Toast.makeText(requireContext(), "Hello toast!", Toast.LENGTH_SHORT).show()
                }

                // Прячем диалог если нажата кнопка "Назад"
                this.findViewById<Button>(R.id.add_task_back_button)?.setOnClickListener {
                    this.findViewById<EditText>(R.id.bottom_sheet_edit_text)?.setText("")
                    dayTime = null
                    it.hideKeyboard()
                    this.hide()
                    hide()
                }
                this.findViewById<ImageButton>(R.id.new_task_set_time)?.setOnClickListener {
                    val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select date")
                        .build()
                    materialDatePicker.addOnPositiveButtonClickListener {
                        dayTime = materialDatePicker.selection
                        val materialTimePicker = MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .build()
                        materialTimePicker.addOnPositiveButtonClickListener {
                            val newHour: Int = materialTimePicker.hour
                            val newMinute: Int = materialTimePicker.minute
                            val plus = (newHour * 3600000) + (newMinute * 60000)
                            dayTime = dayTime?.plus(plus)
//                            Toast.makeText(requireContext(), "newHour = $newHour, newMinute = $newMinute", Toast.LENGTH_LONG).show()
                        }
                        materialTimePicker.show(childFragmentManager, "fragment_time_picker_tag")
                    }
                    materialDatePicker.show(childFragmentManager, "fragment_date_picker_tag")
                }
            }
        }

        val categoryEditorButton: AppCompatImageButton = view.findViewById(R.id.categories_button)
        categoryEditorButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_categoryEditorFragment)
        }

        val mDrawerLayout = requireActivity().findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val appBarButton: BottomAppBar = view.findViewById(R.id.bottom_app_bar)
        val notificationWorker: WorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(4, TimeUnit.SECONDS).build()
        appBarButton.setNavigationOnClickListener {
            WorkManager
                .getInstance(requireContext())
                .enqueue(notificationWorker)
//            view.findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
//            mDrawerLayout.openDrawer(GravityCompat.START);
        }


        val reminderButton: ImageButton = view.findViewById(R.id.home_fragment_reminder_button)
        reminderButton.setOnClickListener {
            val message = "Делай красиво! " + elapsedRealtime()
            val intent = Intent(context, ReminderActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, message)
            }
            startActivity(intent)
        }
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

}