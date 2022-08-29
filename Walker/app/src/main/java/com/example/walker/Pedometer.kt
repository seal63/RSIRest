package com.example.walker

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask


class Pedometer : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var takenSteps = 0
    private var stepSensor: Sensor? = null
    private var exerciseFinished = false
    private var started = false

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeScreen()
        resetSteps()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        listenForTouchToStart()
        runVibrateThread();
    }

    private fun initializeScreen() {
        val win: Window = window
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        exerciseFinished = false;

        setContentView(R.layout.activity_podometer)
    }

    private fun listenForTouchToStart() {
        val view = findViewById<ConstraintLayout>(R.id.pedometer_view) as ConstraintLayout
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                started = true;
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!exerciseFinished) {
            updateStepTaken()
            if (takenSteps >= 50) {
                finishExercise(takenSteps)
            }
        }
    }

    private fun updateStepTaken() {
        started = true;
        var textViewStepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        takenSteps = textViewStepsTaken.text.toString().toInt() + 1
        textViewStepsTaken.text = takenSteps.toString();
    }

    private fun finishExercise(takenSteps: Int) {
        exerciseFinished = true;
        resetSteps()
        returnsToInitialScreen()
        finish();
    }

    private fun returnsToInitialScreen() {
        val intent = Intent(this, MainActivity::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("result", "2");

        setResult(2, intent);
        startActivity(intent);
    }

    private fun resetSteps() {;
        var tvStepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        tvStepsTaken.text = "0";
        takenSteps = 0;
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun runVibrateThread() {
        val task: FutureTask<Void?> = FutureTask(Callable<Void?> {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrateUntilStarted(vibrator)
            null;
        })
        Thread(task).start()
    }

    private fun vibrateUntilStarted(vibrator: Vibrator) {
        if (Build.VERSION.SDK_INT >= 26) {
            while (!started) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                Thread.sleep(3000);
            }
        } else {
            while (!started) {
                vibrator.vibrate(1000)
                Thread.sleep(3000);
            }
        }

    }

}