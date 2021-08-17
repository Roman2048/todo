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
        val builder = NotificationCompat.Builder(context, "planner_nc_1000")
//            .setVibrate(longArrayOf(700L))
            .setSmallIcon(R.drawable.ic_icon_mini_1)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(inputData.getString("content"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        with(NotificationManagerCompat.from(context)) {
            notify(Random.nextInt(), builder.build())
        }
        return Result.success()
    }
}