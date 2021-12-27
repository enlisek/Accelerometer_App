package com.example.accelerometerapp

data class LocationWithAcceleration(
    val latitude:Double, val longitude:Double,
    val altitude:Double, val accuracyLoc: Float,
    val speed:Float,
    val accX:Float, val accY:Float, val accZ:Float
                                    )
