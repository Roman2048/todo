package net.longday.planner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.widget.Button
import android.widget.TextView

class ReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        findViewById<TextView>(R.id.reminder_text_main).text = intent.getStringExtra(EXTRA_MESSAGE)
        val doneButton = findViewById<Button>(R.id.reminder_done_button)
        val laterButton = findViewById<Button>(R.id.reminder_remind_later_button)
        doneButton.setOnClickListener { finish() }
        laterButton.setOnClickListener { finish() }
    }
}