package net.longday.planner.work

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat.DEFAULT_CHANNEL_ID
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.longday.planner.R
import kotlin.random.Random

//
//class NotificationWorker(appContext: Context, workerParams: WorkerParameters):
//    Worker(appContext, workerParams) {
//    override fun doWork(): Result {
//        Handler(Looper.getMainLooper()).post {
//            Log.i("TAG", "ToastToast")
//            Toast.makeText(this.applicationContext, "Toast", Toast.LENGTH_SHORT).show()
//        }
//        return Result.success()
//    }
//}

class OneTimeScheduleWorker(
    val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val builder = NotificationCompat.Builder(context, "test_planner_reminders")
            .setSmallIcon(R.drawable.ic_outline_send_24)
            .setContentTitle("Planner reminder")
            .setContentText("U r the best!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            notify(Random.nextInt(), builder.build())
        }
        return Result.success()
    }

}