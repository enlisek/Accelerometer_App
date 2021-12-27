package com.example.myflicks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.accelerometerapp.JourneyRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel(application: Application):AndroidViewModel(application) {
    val database = Firebase.database
    val myRef = database.getReference("test1")
    val auth = FirebaseAuth.getInstance()

    var listOfJourneys: ArrayList<JourneyRow> = ArrayList()

    var journeyName = ""
    var journeyAdditionalComments: String? = null

}