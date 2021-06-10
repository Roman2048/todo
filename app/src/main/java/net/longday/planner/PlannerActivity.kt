package net.longday.planner

import android.content.Intent
import android.os.SystemClock
import android.provider.AlarmClock
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlannerActivity : AppCompatActivity(R.layout.activity_planner) {

    fun performActions(menuItem: MenuItem) {
        val mDrawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val message = "Планировщик №1"
        val intent = Intent(this, ReminderActivity::class.java).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
        }
        startActivity(intent)
        mDrawerLayout.close()
    }
}