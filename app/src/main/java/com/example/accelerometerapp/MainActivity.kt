package com.example.accelerometerapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.fragment_recording.*
import com.google.android.gms.location.*
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var square: TextView
    private var ifRecord = false
    private lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var request: LocationRequest
    private val database = Firebase.database
    val myRef = database.getReference("test1")
    private val  auth= FirebaseAuth.getInstance()
    private fun setupLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.applicationContext)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1000
            )
        }
    }
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_recording)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        login()
//        print(auth.currentUser!!.email)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        square = findViewById(R.id.tv_square)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        RecordingButton.setOnClickListener {
            if(!ifRecord){
                setUpSensorStuff();
                setupLocationStuff()
                ifRecord = true;
            }
            else {
                sensorManager.unregisterListener(this)
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                ifRecord = false
            }
        }


    }
    fun login(){
        val email="test2@gmail.pl"
        val password="Zaq12wsx"
//        auth.signInAnonymously()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

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

    private fun setupLocationRequest(){
        request = LocationRequest()
        request.interval = 5000
        request.fastestInterval = 5000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    }

    private fun setupLocationStuff(){

        val request = LocationRequest()
        request.interval = 1000
        request.fastestInterval = 1000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        setupLocation()
        val routeID = Random.nextInt().toString()

        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        var locationWithAccelerationCache = arrayOf<LocationWithAcceleration>()
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val userID = auth.currentUser!!.uid
                    val location: Location? = locationResult.lastLocation
                    val list = locationWithAccelerationCache.toMutableList()
                    list.add(LocationWithAcceleration(location!!.latitude,location!!.longitude,
                        location!!.altitude,location!!.accuracy,location!!.speed,sides,upDown,gora
                    ))
                    locationWithAccelerationCache = list.toTypedArray()
                    tv_gps.text = "alt = ${location!!.altitude}\n" +
                            "long = ${location!!.longitude }\n" +
                            "lat = ${location!!.latitude }\n" +
                            "speed = ${location!!.speed }\n" +
                            "acc = ${location!!.accuracy }\n"
                    square.text = "góra/dół ${Math.round(upDown*10)/10.0}\nprawo/lewo ${Math.round(sides*10)/10.0}\nprzód/tył ${Math.round(gora*10)/10.0}"
                    sides = 0.0f
                    upDown = 0.0f
                    gora = 0.0f
                    if(locationWithAccelerationCache.size == 10){
                        myRef.child(userID).child(routeID).child(System.currentTimeMillis().toString()).setValue(locationWithAccelerationCache.toList())
                        locationWithAccelerationCache = arrayOf<LocationWithAcceleration>()
                    }
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, null)}
        else{
            tv_gps.text = "Nie ma połączenia"
        }

    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }
    var sides = 0.0f
    var upDown = 0.0f
    var gora = 0.0f

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            //obtain only the largest bumps over the time interval
                if (abs(event.values[0]) > abs(sides))
                {
                    sides = event.values[0]
                }
                if (abs(event.values[1]) > abs(upDown))
                {
                    upDown = event.values[1]
                }

                if (abs(event.values[2]) > abs(gora))
                {
                    gora = event.values[2]
                }



            //tu Roman dodaje kod zapisujący do bazy
//            wcale nie tutaj
        }
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

}