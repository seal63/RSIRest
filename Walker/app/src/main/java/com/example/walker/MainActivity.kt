package com.example.walker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private var alarm: AlarmManager? = null;

    private var timerHandler = Handler();

    private lateinit var sharedPreferences: SharedPreferences;

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onNewIntent(intent);
        prepareButtonListeners()
        sharedPreferences = getSharedPreferences("globalData", Context.MODE_PRIVATE);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun prepareButtonListeners() {
        val pedometerButton: Button = findViewById(R.id.button_go_podometer)
        pedometerButton.setOnClickListener {
            prepareNextAlarm()
        }

        /*
        val button: Button = findViewById(R.id.button_go_calendar)
        button.setOnClickListener {
            val intent = Intent(this@MainActivity, CalendarActivity::class.java)
            startActivity(intent)
        }
        */

    }

    private fun refreshTotalSteps() {
        var textViewTotalSteps = findViewById<TextView>(R.id.tv_total_steps);
        textViewTotalSteps.text = (getStoredIntKey("totalSteps")).toString();
    }

    private fun getStoredIntKey(key: String): Int {
        return sharedPreferences.getInt(key, 0);
    }

    private fun setStoredIntKey(key: String, value: Int) {
        val editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private fun updateTotalSteps() {
        val newTotalSteps = getStoredIntKey("totalSteps") + 50;
        setStoredIntKey("totalSteps", newTotalSteps)
        var textViewTotalSteps = findViewById<TextView>(R.id.tv_total_steps);
        textViewTotalSteps.text = (newTotalSteps).toString();
        //saveStepProgress(50);

        //var textViewTotalHours = findViewById<TextView>(R.id.tv_total_hours);
        //textViewTotalHours.text = getStepProgress()
    }

    private fun getStepProgress(): String {
        val mapDayHours: MutableMap<Map<String, Int>, Int> = getMapDayHoursMap()
        var totalHourSteps: String = "";
        mapDayHours.forEach { entry ->
            totalHourSteps = totalHourSteps + entry.key["hours"].toString() +
                    ":" + entry.key["minutes"].toString() + "steps " + entry.value.toString()
        }
        return totalHourSteps
    }
    private fun saveStepProgress(stepsTaken: Int){
        var stepsRecord: String = Gson().toJson(getMapYears())
        var editor = sharedPreferences.edit();
        editor.putString("stepsRecord", null);
        editor.commit();
        if(!sharedPreferences.contains("stepsRecord")){
            initializeYearMonthsMap()
        }
        val rightNow: Calendar = Calendar.getInstance()
        val mapDayHours: MutableMap<Map<String, Int>, Int> = getMapDayHoursMap()


        val date: MutableMap<String, Int>  = HashMap<String, Int>();
        date["hours"] =  rightNow.get(Calendar.HOUR_OF_DAY)
        date["minutes"] =  rightNow.get(Calendar.MINUTE)

        mapDayHours[date] = stepsTaken
        stepsRecord = Gson().toJson(getMapYears())
        editor = sharedPreferences.edit();
        editor.putString("stepsRecord", stepsRecord);
        editor.commit();
    }

    private fun getMapYears() :MutableMap<Int, Map<Int, Map<Int, Map<Map<String, Int>, Int>>>>{
        val mapYearMonthsString: String = sharedPreferences.getString("stepsRecord", "{}")!!
        val mapYearMonthsAny: MutableMap<*, *>? =
            Gson().fromJson(mapYearMonthsString, MutableMap::class.java)
        return mapYearMonthsAny as MutableMap<Int, Map<Int, Map<Int, Map<Map<String, Int>, Int>>>>
    }
    private fun getMapDayHoursMap() :MutableMap<Map<String, Int>, Int>{
        val mapYearsString: String = sharedPreferences.getString("stepsRecord", "{}")!!
        val mapYearsAny: MutableMap<*, *>? =
            Gson().fromJson(mapYearsString, MutableMap::class.java)

        val mapYears:  MutableMap<Int, Map<Int, Map<Int, Map<Map<String, Int>, Int>>>> =
            mapYearsAny as  MutableMap<Int, Map<Int, Map<Int, Map<Map<String, Int>, Int>>>>
        val rightNow: Calendar = Calendar.getInstance()
        val year = rightNow.get(Calendar.YEAR)
        val month = rightNow.get(Calendar.MONTH)
        val day = rightNow.get(Calendar.DAY_OF_MONTH)
        return mapYears[year]!![month]!![day]!!.toMutableMap()
    }
    private fun initializeYearMonthsMap() {

        val mapYears: MutableMap<Int, Map<Int, Map<Int, Map<Map<String, Int>, Int>>>>
        = HashMap<Int, Map<Int, Map<Int, Map<Map<String, Int>, Int>>>>()

        val mapYearMonths: MutableMap<Int, Map<Int, Map<Map<String, Int>, Int>>> =
                HashMap<Int, Map<Int, Map<Map<String, Int>, Int>>>()
        val rightNow: Calendar = Calendar.getInstance()
        val year = rightNow.get(Calendar.YEAR)

        for (i in 1..12) {
            val mapMonthDays: MutableMap<Int, Map<Map<String, Int>, Int>> =
                HashMap<Int, Map<Map<String, Int>, Int>>()
            fillMonthDaysMap(mapMonthDays, i)
            mapYearMonths[i] = mapMonthDays
        }

        mapYears[year] =  mapYearMonths
        val stepsRecord: String = Gson().toJson(mapYears)
        val editor = sharedPreferences.edit();
        editor.putString("stepsRecord", stepsRecord);
        editor.commit();
    }

    private fun fillMonthDaysMap(mapMonthDays: MutableMap<Int, Map<Map<String, Int>, Int>>, monthNumber: Int){
        val calendar: Calendar = Calendar.getInstance()
        val maxDay: Int = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for(day in 1..maxDay){
            val mapDayHours: Map<Map<String, Int>, Int>
                    = HashMap<Map<String, Int>, Int>()
            mapMonthDays[day] = mapDayHours
        }
    }

    private fun saveNextAlarmTime(time: Long) {
        val editor = sharedPreferences.edit();
        editor.putLong("nextAlarmTime", time);
        editor.commit();
    }

    private fun getNextAlarmTime(): Long {
        return sharedPreferences.getLong("nextAlarmTime", 0);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun prepareNextAlarm() {
        alarm = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, Pedometer::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pi = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        val nextAlarmTime = getNextAlarmTimestamp();
        val alarmInfo = AlarmManager.AlarmClockInfo(nextAlarmTime, pi);
        saveNextAlarmTime(nextAlarmTime)
        alarm!!.setAlarmClock(alarmInfo, pi);

    }

    private fun getNextAlarmTimestamp(): Long {
        val minutes = 25;
        val seconds = minutes * 60;
        val milliseconds = seconds * 1000;
        return System.currentTimeMillis() + milliseconds;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        sharedPreferences = getSharedPreferences("globalData", Context.MODE_PRIVATE);
        val extras = intent.extras;
        if (extras != null) {
            val value = extras.getString("result")
            extras.putString("result", null);
            if (value.equals("2")) {
                updateTotalSteps();
                prepareNextAlarm();
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("globalData", Context.MODE_PRIVATE);
        refreshTotalSteps();

        updateNextTimeView()
        updateTime();
    }

    private fun updateNextTimeView() {
        val millisecondsForTrigger = getNextAlarmTime() - System.currentTimeMillis();
        var secondsForTrigger = millisecondsForTrigger / 1000
        val minutesForTrigger = secondsForTrigger / 60
        secondsForTrigger = secondsForTrigger % 60

        var tvNextTime = findViewById<TextView>(R.id.tv_next_time);

        tvNextTime.text = minutesForTrigger.toString() + ":" + secondsForTrigger.toString();
    }

    var updater: Runnable? = null

    private fun updateTime() {
        timerHandler = Handler()
        updater = Runnable {
            updateNextTimeView()
            updater?.let { timerHandler.postDelayed(it, 1000) }
        }
        timerHandler.post(updater!!)
    }

    override fun onPause() {
        super.onPause()
        updater?.let { timerHandler.removeCallbacks(it) }
    }
}