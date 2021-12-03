package com.example.accelerometerapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.fragment_recording.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var square: TextView
    private var ifRecord = false

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_recording)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        square = findViewById(R.id.tv_square)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        RecordingButton.setOnClickListener {
            if(!ifRecord){
                setUpSensorStuff();
                ifRecord = true;
            }
            else {
                sensorManager.unregisterListener(this)
                ifRecord = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun setUpSensorStuff() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this,it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val sides = event.values[0]
            val upDown = event.values[1]
            val gora = event.values[2]

            square.text = "góra/dół ${Math.round(upDown*10)/10.0}\nprawo/lewo ${Math.round(sides*10)/10.0}\nprzód/tył ${Math.round(gora*10)/10.0}"

            //tu Roman dodaje kod zapisujący do bazy
        }
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

}