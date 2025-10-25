package com.lab.crowdcheck.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.lab.crowdcheck.domain.model.Korisnik
import com.lab.crowdcheck.domain.model.Poi
import com.lab.crowdcheck.domain.model.Popunjenost
import com.lab.crowdcheck.domain.repository.PoiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil

class PoiRepoImplementation(
    private val database : FirebaseFirestore
) : PoiRepo {
    private val poiCollection = database.collection("pois")
    
    companion object{
        private const val TAG = "[PoiRepo]"
    }

    private val mogucePopunjenosti = Popunjenost.entries.map { it.name}


    override suspend fun sacuvajPoi(poi : Poi) : Result<Unit>{
        try{
            poiCollection.document().set(poi).await()
            Log.d(TAG,"Uspesno sacuvan POI u bazu!")
            return Result.success(Unit)
        }
        catch(e : Exception)
        {
            Log.e(TAG,"Doslo je do greske prilikom cuvanja POI : ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }
    override suspend fun sacuvajPoi( userId : String ,
                            opis : String ,
                            urlSlike : String ,
                            popunjenost : String ,
                            tipLokala : String ,): Result<Unit>{
        try{
            if(!mogucePopunjenosti.contains(popunjenost))
                throw Exception("Nevalidna popunjenost : $popunjenost")

            val mapaAtributa = mapOf(
                "userId" to userId,
                "opis" to opis,
                "urlSlike" to urlSlike,
                "popunjenost" to popunjenost,
                "tipLokala" to tipLokala,

            )

            Log.d(TAG,"Uspesno sacuvan POI!")
            return Result.success(Unit)
        }
        catch(e : Exception)
        {
            Log.e(TAG,"Doslo je do greske prilikom cuvanja POI : ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    //override suspend fun vratiPoi(userId : String,id : String)
    //override suspend fun izmeniPoi(id : String)
    override suspend fun izbrisiPoi(id : String): Result<Unit>{
        try{
            poiCollection.document(id).delete().await()

            Log.d(TAG,"Uspesno izbrisan POI!")
            return Result.success(Unit)
        }
        catch(e : Exception)
        {
            Log.e(TAG,"Doslo je do greske prilikom brisanja POI : ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override suspend fun izmeniOpis(id : String, noviOpis : String): Result<Unit>{
        try{
            val izmeneMap = mapOf(
                "opis" to noviOpis
            )

            poiCollection.document(id).update(izmeneMap).await()

            Log.d(TAG,"Uspesno izmenjen opis POI-a!")
            return Result.success(Unit)
        }
        catch(e : Exception)
        {
            Log.e(TAG,"Doslo je do greske prilikom menjanaj opisa POI-a : ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }
    override suspend fun izmeniPopunjenost(id : String, novaPopunjenost : String): Result<Unit>{
        try{
            if(!mogucePopunjenosti.contains(novaPopunjenost))
                throw Exception("Nevalidna nova popunjenost: $novaPopunjenost")

            val izmeneMap = mapOf(
                "popunjenost" to novaPopunjenost
            )

            poiCollection.document(id).update(izmeneMap).await()

            Log.d(TAG,"Uspesno sacuvan PLACEHOLDER!")
            return Result.success(Unit)
        }
        catch(e : Exception)
        {
            Log.e(TAG,"Doslo je do greske prilikom PLACEHOLDER : ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override suspend fun oceniPoi(id : String, idKorisnika : String, ocena : Int) : Result<Unit>{
        try{
            if(ocena !in 1..10)
                throw Exception("Ocena mora biti izmedju 1-10. Ocena je: $ocena")

            val poiRef = poiCollection.document(id)
            val korisnikRef = database.collection("users").document(idKorisnika)

            //u ovoj transakciji mora istovremeno da se azurira ocena POI-a
            //kao i da se azuriraju poeni kod korisnika
            database.runTransaction{ transaction ->

                val poiSnapshot = transaction.get(poiRef)
                if(!poiSnapshot.exists())
                    throw Exception("POI sa zadatim id-jem ne postoji")
                val poi = poiSnapshot.toObject(Poi::class.java)!!

                if(poi.userId == idKorisnika)
                    throw Exception("Korisnik ne moze oceniti sopstveni POI!!!")

                val korisnikSnapshot = transaction.get(korisnikRef)
                if(!korisnikSnapshot.exists())
                    throw Exception("Korisnik sa zadatim id-jem ne postoji")
                val korisnik = korisnikSnapshot.toObject(Korisnik::class.java)!!


                //racunanje nove prosecne ocene (neprecizno zbog ovaj ceil ali bolje to
                // nego da se majem sa jos jedan field)
                val ukupno = ceil(poi.brojOcena * poi.prosecnaOcena) + ocena

                val poiNoviBrojOcena = poi.brojOcena + 1
                val poiNoviProsek  = ukupno / poiNoviBrojOcena

                val poiUpdate = mapOf(
                    "prosecnaOcena" to poiNoviProsek,
                    "brojOcena" to poiNoviBrojOcena,
                    "vremeIzmene" to FieldValue.serverTimestamp()
                )

                //val vremeUpdate = mapOf("vremeIzmene" to FieldValue.serverTimestamp())

                val korisnikNoviPoeni = korisnik.poeni + ocena
                val korisnikUpdate = mapOf(
                    "poeni" to korisnikNoviPoeni
                )

                transaction.update(korisnikRef,korisnikUpdate)
                transaction.update(poiRef,poiUpdate)
                //transaction.update(poiRef,vremeUpdate,SetOptions.merge())
                null
            }.await()

            Log.d(TAG,"Uspesno ocenjen POI!")
            return Result.success(Unit)
        }
        catch(e : Exception)
        {
            Log.e(TAG,"Doslo je do greske prilikom ocenjivanja POI-a : ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    override suspend fun vratiSvePoisKorisnika(uidKorisnika : String) : Flow<Result<List<Poi>>>{
        TODO("")
    }
    override suspend fun vratiSvePoisPoBlizini(geopoint : GeoPoint) : Flow<Result<List<Poi>>>{
        TODO("")
    }
}