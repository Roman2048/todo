package net.longday.planner

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlannerActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planner)

        drawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> navigateWithMessage("Планировщик №1")
                R.id.settings_item -> navigateWithMessage("Планировщик №2")
                else -> navigateWithMessage("Планировщик №0")
            }
            true
        }
    }

    private fun navigateWithMessage(message: String) {
        startActivity(
            Intent(this, ReminderActivity::class.java).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
            }
        )
        drawerLayout.close()
    }
}