package com.example.accelerometerapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.fragment_recording.*
import kotlin.math.abs
import kotlin.math.roundToInt


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
    private lateinit var intent: Intent
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
        mainViewModel = ViewModelProvider(
                requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(
                Application()
        )
        ).get(MainViewModel::class.java)

        setHasOptionsMenu(true)
        intent = Intent(context, MyService::class.java)

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
                RecordingButton.text = "Pause recording"
            }
            else {
                sensorManager.unregisterListener(this)
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                ifRecord = false
                RecordingButton.text = "Start recording"
            }
        }

        FinishButton.setOnClickListener {
            sensorManager.unregisterListener(this)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            ifRecord = false
            view.findNavController().navigate(R.id.action_recordingFragment_to_mainViewFragment)
        }

    }


    private lateinit var mService: MyService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MyService.LocalBinder
            mService = binder.getService(mainViewModel.userId, mainViewModel.routeID, mainViewModel.myRef)
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
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
    public fun setupLocationStuff(){

        val request = LocationRequest()
        request.interval = 1000
        request.fastestInterval = 1000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        setupLocation()

        val permission = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )
        var locationWithAccelerationCache = arrayOf<LocationWithAcceleration>()
        if (permission == PackageManager.PERMISSION_GRANTED) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val userID = mainViewModel.userId
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
                    tv_gps.text = "GPS\n" +
                            "alt = ${(location!!.altitude * 1000).roundToInt() /1000.0}\n" +
                            "long = ${(location!!.altitude * 1000).roundToInt() /1000.0}\n" +
                            "lat = ${(location!!.latitude * 1000).roundToInt() /1000.0}\n" +
                            "speed = ${(location!!.speed * 1000).roundToInt() /1000.0}\n" +
                            "acc = ${(location!!.accuracy * 1000).roundToInt() /1000.0}\n"
                    tv_acceletometer.text = "Acceleration\n" +
                            "X axis: ${(accX * 10).roundToInt() /10.0}\n" +
                            "Y axis: ${(accY * 10).roundToInt() /10.0}\n" +
                            "Z axis: ${(accZ * 10).roundToInt() /10.0}"
                    accX = 0.0f
                    accY = 0.0f
                    accZ = 0.0f
                    if(locationWithAccelerationCache.size == 10){
                        mainViewModel.myRef.child(userID).child(mainViewModel.routeID).child(
                                System.currentTimeMillis().toString()
                        ).setValue(locationWithAccelerationCache.toList())
                        locationWithAccelerationCache = arrayOf<LocationWithAcceleration>()
                    }
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, null)}
        else{
            tv_gps.text = "Connection broken"
        }
    }

    override fun onPause() {
        super.onPause()
        if(ifRecord) {
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
        mBound = false
        if (ifRecord) {
            requireContext().unbindService(connection)
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
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {
                mainViewModel.auth.signOut()
                requireView().findNavController().navigate(R.id.action_recordingFragment_to_loginFragment)
                return true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.top_app_bar, menu)
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