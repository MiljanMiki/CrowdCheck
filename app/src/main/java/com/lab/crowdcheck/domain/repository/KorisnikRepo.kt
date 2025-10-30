package com.lab.crowdcheck.domain.repository

import com.lab.crowdcheck.domain.model.Korisnik
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface KorisnikRepo {
    suspend fun sacuvajKorisnika( korisnik : Korisnik) : Result<Unit>
    //suspend fun izmeniKorisnika( uid : String) : Result<Unit>
    suspend fun vratiKorisnika(uid: String) : Flow<Result<Korisnik>>
    //suspend fun izbrisiKorisnika(uid: String) : Result<Unit>

    suspend fun nadjiKorisnikaPoUsername(username : String) : Result<Korisnik>
    suspend fun nadjiUIDPoUsername(username : String) : Result<String>

    suspend fun dodajPoene(uid : String, poeni : Int) : Result<Unit>

    //sortirano po poenima, za buducu rang listu ako bude
    suspend fun vratiKorisnikePoPoenima(limitKorisnika : Long = 100) : Flow<Result<List<Korisnik>>>

    suspend fun vratiKorisnikePoBlizini(limitKorisnika : Long = 100) : Flow<Result<List<Korisnik>>>
}