package com.lab.crowdcheck.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.lab.crowdcheck.domain.model.Korisnik
import com.lab.crowdcheck.domain.repository.KorisnikRepo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class KorisnikRepoImplementation(
    private val database : FirebaseFirestore
) : KorisnikRepo {

    private val usersCollection = database.collection("users")
    private val usernamesCollection = database.collection("usernames")

    companion object{
        private const val TAG = "[KorisnikRepo]"
    }

    private fun validanBrojTelefona(brojTelefona : String) : String?{
        if(brojTelefona.any{ !it.isDigit()})
            return "Broj telefona moze sadrzati samo brojeve!"
        return null
    }

    override suspend fun sacuvajKorisnika(korisnik : Korisnik) : Result<Unit>{
        try{

            val validanBroj = validanBrojTelefona(korisnik.brojTelefona)
            if(validanBroj != null)
                throw Exception(validanBroj)

            val normalizedUsername = korisnik.username.lowercase()
            val usernameRef = usernamesCollection.document(normalizedUsername)
            val userDocRef = usersCollection.document(korisnik.uid)

            database.runTransaction { transaction ->
                val snapshot = transaction.get(usernameRef)

                if (snapshot.exists()) {
                    throw Exception("Username '${korisnik.username}' je vec zauzeto!.")
                }

                transaction.set(usernameRef, mapOf("uid" to korisnik.uid))

                transaction.set(userDocRef, korisnik)
                null // Success
            }.await()
            return Result.success(Unit)
        }
        catch(e: Exception)
        {
            Log.e(TAG,"Greska prilikom ubacivanja korisnika u bazu: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }

    }
    //override suspend fun izmeniKorisnika( uid : String,) : Result<Unit>{}


    override suspend fun vratiKorisnika(uid: String) : Flow<Result<Korisnik>> = callbackFlow{
        val korisnikRef = usersCollection.document(uid)

        val listener = korisnikRef.addSnapshotListener{ snapshot, error ->
            if(error!=null)
            {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            if(snapshot != null && snapshot.exists())
            {
                try {
                    val korisnik = snapshot.toObject(Korisnik::class.java)
                    if (korisnik != null)
                        trySend(Result.success(korisnik))
                    else
                        trySend(Result.failure(Exception("Greska prilikom pretvaranja korisnika u objekat")))
                }
                catch(e : Exception)
                {
                    Log.e(TAG,"Greska prilikom prevodjenja snapshot-a u objekat : ${e.message}")
                    e.printStackTrace()
                    trySend( Result.failure(e))
                }
            }
            else
            {
                trySend( Result.failure(Exception("Ne postoji korisnik sa datim uid")))
            }
        }

        awaitClose{ listener.remove()}
    }
    //override suspend fun izbrisiKorisnika(uid: String) : Result<Unit>

    override suspend fun nadjiKorisnikaPoUsername(username : String) : Result<Korisnik>{
        try{
            val usernameSnapshot = usernamesCollection.document(username.lowercase()).get().await()
            if(usernameSnapshot.exists()) {
                val data = usernameSnapshot.data
                val uid = data?.get("uid") as String

                val korisnikRef = usersCollection.document(uid).get().await()

                if(korisnikRef!= null && korisnikRef.exists())
                {
                    val korisnik = korisnikRef.toObject(Korisnik::class.java)
                    if(korisnik!=null)
                        return Result.success(korisnik)
                    else
                        return Result.failure(Exception("Greska prilikom pretvaranja korisnika u objekat"))
                }
                else
                    return Result.failure(Exception("Ne postoji korisnik sa datim username-om"))
            }
            else
                throw Exception("Ne postoji korisnik sa datim username-om!")
        }
        catch(e : Exception)
        {
            Log.e(TAG,"Doslo je do greske prilikom vracanja korisnika po username : ${e.message}")
            e.printStackTrace()
            return Result.failure(e);
        }

    }
    override suspend fun nadjiUIDPoUsername(username : String) : Result<String>{
        try{
            val usernameSnapshot = usernamesCollection.document(username.lowercase()).get().await()
            if(usernameSnapshot.exists()) {
                val data = usernameSnapshot.data

                val uid = data?.get("uid") as String

                return Result.success(uid)
            }
            else
                throw Exception("Ne postoji korisnik sa datim username-om!")
        }
        catch(e : Exception)
        {
            Log.e(TAG,"Doslo je do greske prilikom vracanja korisnika po username : ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    //redundantno
    override suspend fun dodajPoene(uid : String, poeni : Int) : Result<Unit>{
        val korisnikRef = usersCollection.document(uid)

        try{
            database.runTransaction { transaction ->
                val userSnapshot = transaction.get(korisnikRef)

                if (!userSnapshot.exists()) {
                    throw Exception("Korisnik sa zadatim id-jem ne postoji!.")
                }

                val user = userSnapshot.toObject(Korisnik::class.java)!!

                val noviPoeni = user.poeni + poeni


                val userUpdates = hashMapOf<String, Any>(
                    "poeni" to noviPoeni,
                )
                transaction.update(korisnikRef, userUpdates)

                null
            }.await()

            Log.d(TAG, "Uspesno ocenjen korisnik sa id-jem: $uid.")
            return Result.success(Unit)
        }
        catch(e:Exception)
        {
            Log.e(TAG,"Greska prilikom dodavanja poena korisniku : ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    //sortirano po poenima, za buducu rang listu ako bude
    override suspend fun vratiKorisnikePoPoenima(limitKorisnika : Long) : Flow<Result<List<Korisnik>>> = callbackFlow{

        val query : Query
        query = usersCollection.orderBy("poeni",Query.Direction.DESCENDING).limit(limitKorisnika)

        val listener = query.addSnapshotListener{ snapshot, error ->
            if(error!=null)
            {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            if(snapshot!=null && !snapshot.isEmpty)
            {
                try {
                    val korisnici =
                        snapshot.documents.mapNotNull { it.toObject(Korisnik::class.java) }

                    if(korisnici.isNotEmpty())
                        trySend(Result.success(korisnici))
                    else
                        throw Exception("Rezultirajuci vraceni korisnici su prazni!")
                }
                catch(e : Exception) {
                    Log.e(TAG, "Doslo je do greske prilikom mapiranja korisnika : ${e.message}")
                    e.printStackTrace()
                    trySend(Result.failure(e))
                }
            }
            else
                trySend(Result.failure(Exception("Query nije vratio nista!")))
        }
        awaitClose{listener.remove()}
    }

    override suspend fun vratiKorisnikePoBlizini(limitKorisnika : Long) : Flow<Result<List<Korisnik>>>{
        TODO("Idalje sam nesposoban za mapu")
    }
}