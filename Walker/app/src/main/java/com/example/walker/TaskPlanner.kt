package com.example.walker

import android.R
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.PendingIntent.getActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService


class TaskPlanner : BroadcastReceiver(){

    private var alarm: AlarmManager? = null;

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
            val pedometerActivity = Intent(context, Pedometer::class.java)

            pedometerActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            val pendingIntent = getActivity(context, 0, pedometerActivity,  FLAG_ONE_SHOT );

        val noti: Notification = Notification.Builder(context)
                .setContentTitle("title")
                .setContentText("text")
                .setFullScreenIntent (pendingIntent, true)
                //.setSmallIcon(R.drawable.new_mail)
                //.setLargeIcon(aBitmap)
                .build()

              ;
            //context!!.startActivity(pedometerActivity)
    }

}
