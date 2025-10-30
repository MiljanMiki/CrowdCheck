package com.lab.crowdcheck

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.runner.RunWith
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

// Firebase Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.lab.crowdcheck.data.remote.firebase.AuthenticationEmail

// Coroutine test utilities
import kotlinx.coroutines.test.runTest

// Your app class (replace with your actual package and class)

@RunWith(AndroidJUnit4::class)
class TestAuthServicePassword
{
    private lateinit var auth:FirebaseAuth
    private lateinit var service:AuthenticationEmail

    @Before
    fun setUp()
    {
        auth=FirebaseAuth.getInstance()
        //auth.useEmulator("10.0.2.2",9099)
        service=AuthenticationEmail(auth)
    }

    @Test
    fun mainLoginFlowTest()=runTest()
    {
        val email="test@email.com"
        var password="password"

        //LOSA SIFRA
//        var napraviResult = service.napraviKorisnika(email,password)
//        assertNotNull((napraviResult.getOrNull()))
//        assertTrue(napraviResult.isFailure)


        //ok sifra
        password = "Jasam_siFRA111!_#"

        var napraviResult = service.napraviKorisnika(email,password)
        assertNotNull((napraviResult.getOrNull()))
        assertTrue(napraviResult.isSuccess)

        //ulogujemo ga
        val signInResult=service.ulogujKorisnika(email,password)
        assertTrue(signInResult.isSuccess)
        assertEquals(email,signInResult.getOrNull()?.email)

        assertNotNull(service.vratiTrenutnogKorisnika())

        assertTrue(service.daLiJeKorisnikUlogovan())

        //izlogujemo korisnika
        service.izlogujKorisnika()
        assertFalse(service.daLiJeKorisnikUlogovan())
        assertNull(service.vratiTrenutnogKorisnika())

    }
}