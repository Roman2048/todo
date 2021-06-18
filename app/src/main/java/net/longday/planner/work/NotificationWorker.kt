package net.longday.planner.work

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters


class NotificationWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Handler(Looper.getMainLooper()).post {
            Log.i("TAG", "ToastToast")
            Toast.makeText(this.applicationContext, "Toast", Toast.LENGTH_SHORT).show()
        }
        return Result.success()
    }
}