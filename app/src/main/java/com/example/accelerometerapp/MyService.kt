package com.example.accelerometerapp

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_recording.*
import kotlin.math.abs

class MyService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var request: LocationRequest
    private val binder = LocalBinder()
    private lateinit var userId: String
    private lateinit var routeId: String
    private lateinit var ref: DatabaseReference


    inner class LocalBinder : Binder() {
        fun getService(idUser: String, idRoute: String, database: DatabaseReference): MyService {
            userId = idUser
            routeId = idRoute
            ref = database
            return this@MyService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Toast.makeText(
                applicationContext, "Recording in progress...",
                Toast.LENGTH_LONG
        ).show()
        return binder
    }

    var accX = 0.0f
    var accY = 0.0f
    var accZ = 0.0f

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                    this, it,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        setupLocationStuff()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        onTaskRemoved(intent)
        Toast.makeText(
                applicationContext, "This is a Service running in Background",
                Toast.LENGTH_LONG
        ).show()

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            if (abs(event.values[0]) > abs(accX))
            {
                accX = event.values[0]
            }
            if (abs(event.values[1]) > abs(accY))
            {
                accY = event.values[1]
            }

            if (abs(event.values[2]) > abs(accZ))
            {
                accZ = event.values[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    private fun setupLocationStuff(){
        val request = LocationRequest()
        request.interval = 1000
        request.fastestInterval = 1000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val permission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        var locationWithAccelerationCache = arrayOf<LocationWithAcceleration>()
        if (permission == PackageManager.PERMISSION_GRANTED) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location? = locationResult.lastLocation
                    val list = locationWithAccelerationCache.toMutableList()
                    list.add(
                            LocationWithAcceleration(
                                    location!!.latitude,
                                    location!!.longitude,
                                    location!!.altitude,
                                    location!!.accuracy,
                                    location!!.speed,
                                    accX,
                                    accY,
                                    accZ
                            )
                    )
                    locationWithAccelerationCache = list.toTypedArray()
                    accX = 0.0f
                    accY = 0.0f
                    accZ = 0.0f
                    if(locationWithAccelerationCache.size == 10){
                        ref.child(userId).child(routeId).child(
                                System.currentTimeMillis().toString()
                        ).setValue(locationWithAccelerationCache.toList())
                        locationWithAccelerationCache = arrayOf<LocationWithAcceleration>()
                    }
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, null)
        }
    }

    private fun disconnect() {
        sensorManager.unregisterListener(this)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
    override fun stopService(name: Intent?): Boolean {
        this.stopSelf()
        disconnect()
        return super.stopService(name)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        disconnect()
        return super.onUnbind(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        this.stopSelf()
    }
}
