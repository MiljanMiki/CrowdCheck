package com.lab.crowdcheck.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class Korisnik(
    //osnovni podaci
    @DocumentId
    val uid : String = "",
    val username : String ="",
    val ime: String ="",
    val prezime : String ="",
    val brojTelefona: String ="",
    val urlFotografije : String ="",

    val poeni : Int = 0,

    //sve vezano za lokaciju korisnika
    val geohash: String? = null,
    val geopoint : GeoPoint? = null
) {

}