package net.longday.planner.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.longday.planner.R
import kotlin.random.Random

class OneTimeScheduleWorker(
    val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val builder = NotificationCompat.Builder(context, "test_planner_reminders")
            .setVibrate(longArrayOf(400, 200, 200))
            .setSmallIcon(R.drawable.ic_round_notifications_active_24)
            .setContentTitle("Planner reminder:")
            .setContentText(inputData.getString("content"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            notify(Random.nextInt(), builder.build())
        }
        return Result.success()
    }
}