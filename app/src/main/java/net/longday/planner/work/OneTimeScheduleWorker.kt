package net.longday.planner.work

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.longday.planner.PlannerActivity
import net.longday.planner.R
import kotlin.random.Random


class OneTimeScheduleWorker(
    val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val intent = Intent(context, PlannerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 42, intent, 0)
        val builder = NotificationCompat.Builder(context, "planner_nc_1000")
//            .setVibrate(longArrayOf(700L))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_round_alarm_24)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(inputData.getString("content"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        with(NotificationManagerCompat.from(context)) {
            val randomInt = Random.nextInt()
            notify(randomInt, builder.build())
        }
        return Result.success()
    }
}