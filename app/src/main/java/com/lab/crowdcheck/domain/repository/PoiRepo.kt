package com.lab.crowdcheck.domain.repository

import com.google.firebase.firestore.GeoPoint
import com.lab.crowdcheck.domain.model.Poi
import kotlinx.coroutines.flow.Flow

interface PoiRepo {

    suspend fun sacuvajPoi(poi : Poi) : Result<Unit>
    suspend fun sacuvajPoi( userId : String = "",
                            opis : String = "",
                            urlSlike : String = "",
                            popunjenost : String ="",
                            tipLokala : String = "",): Result<Unit>

    //suspend fun vratiPoi(userId : String,id : String)
    //suspend fun izmeniPoi(id : String)
    suspend fun izbrisiPoi(id : String): Result<Unit>

    suspend fun izmeniOpis(id : String, noviOpis : String): Result<Unit>
    suspend fun izmeniPopunjenost(id : String, novaPopunjenost : String): Result<Unit>

    suspend fun oceniPoi(id : String,idKorisnika : String, ocena : Int) : Result<Unit>

    suspend fun vratiSvePoisKorisnika(uidKorisnika : String) : Flow<Result<List<Poi>>>
    suspend fun vratiSvePoisPoBlizini(geopoint : GeoPoint) : Flow<Result<List<Poi>>>
}