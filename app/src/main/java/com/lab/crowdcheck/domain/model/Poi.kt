package com.lab.crowdcheck.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

data class Poi(
    @DocumentId
    val id : String = "",
    val userId : String = "",
    val opis : String = "",
    val urlSlike : String = "",
    val popunjenost : String ="",//enum Popunjenost
    val tipLokala : String = "",

    val prosecnaOcena : Double = 0.0,
    val brojOcena : Int = 0,

    @ServerTimestamp
    val vremeDodavanja : Timestamp? = null,
    @ServerTimestamp
    val vremeIzmene : Timestamp? = null,

    val geohash: String? = null,
    val geopoint : GeoPoint? = null,

) {
}