package com.example.accelerometerapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.accelerometerapp.JourneyRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel(application: Application):AndroidViewModel(application) {

    var journeyName = ""
    var journeyAdditionalComments: String? = null
    val database = Firebase.database
    var userId = ""

    var userRef = database.getReference(userId)
    val auth = FirebaseAuth.getInstance()

    var listOfJourneys: ArrayList<JourneyRow> = ArrayList()
    val myRef = database.getReference("test6")

    var routeID = ""
}