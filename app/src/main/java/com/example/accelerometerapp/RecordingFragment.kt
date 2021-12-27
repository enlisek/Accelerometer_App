package com.example.accelerometerapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myflicks.MainViewModel
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.fragment_recording.*
import kotlin.math.abs
import kotlin.random.Random


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecordingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordingFragment : Fragment(), SensorEventListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var sensorManager: SensorManager
    private lateinit var tv_acceletometer: TextView
    private var ifRecord = false
    private lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var request: LocationRequest
    private lateinit var mainViewModel: MainViewModel
    var accX = 0.0f
    var accY = 0.0f
    var accZ = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mainViewModel = ViewModelProvider(
            requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(
                Application()
            )
        ).get(MainViewModel::class.java)
        return inflater.inflate(R.layout.fragment_recording, container, false)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //login()
        tv_acceletometer = tv_acc
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            requireActivity()
        )

        RecordingButton.setOnClickListener {
            if(!ifRecord){
                setUpSensorStuff();
                setupLocationStuff()
                ifRecord = true;
                RecordingButton.text = "Stop recording"
            }
            else {
                sensorManager.unregisterListener(this)
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                ifRecord = false
                RecordingButton.text = "Start recording"
            }
        }

    }

    private fun setupLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1000
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupLocationStuff(){

        val request = LocationRequest()
        request.interval = 1000
        request.fastestInterval = 1000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        setupLocation()
        val routeID = Random.nextInt().toString()

        val permission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )
        var locationWithAccelerationCache = arrayOf<LocationWithAcceleration>()
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val userID = mainViewModel.auth.currentUser!!.uid
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
                    tv_gps.text = "alt = ${location!!.altitude}\n" +
                            "long = ${location!!.longitude }\n" +
                            "lat = ${location!!.latitude }\n" +
                            "speed = ${location!!.speed }\n" +
                            "acc = ${location!!.accuracy }\n"
                    tv_acceletometer.text = "X axis: ${Math.round(accX * 10)/10.0}\nY axis: ${Math.round(
                        accY * 10
                    )/10.0}\nZ axis: ${Math.round(accZ * 10)/10.0}"
                    accX = 0.0f
                    accY = 0.0f
                    accZ = 0.0f
                    if(locationWithAccelerationCache.size == 10){
                        mainViewModel.myRef.child(userID).child(routeID).child(
                            System.currentTimeMillis().toString()
                        ).setValue(locationWithAccelerationCache.toList())
                        locationWithAccelerationCache = arrayOf<LocationWithAcceleration>()
                    }
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, null)}
        else{
            tv_gps.text = "Nie ma połączenia"
        }

    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun setUpSensorStuff() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setupLocationRequest(){
        request = LocationRequest()
        request.interval = 5000
        request.fastestInterval = 5000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            //obtain only the largest bumps over the time interval
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
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecordingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                RecordingFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}