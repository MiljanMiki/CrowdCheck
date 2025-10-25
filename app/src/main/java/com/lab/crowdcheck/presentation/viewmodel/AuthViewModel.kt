package com.lab.crowdcheck.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.lab.crowdcheck.data.remote.firebase.AuthenticationEmail
import com.lab.crowdcheck.data.remote.firebase.bezbednaSifra
import com.lab.crowdcheck.data.remote.firebase.validanEmail
import com.lab.crowdcheck.data.repository.KorisnikRepoImplementation
import com.lab.crowdcheck.domain.model.Korisnik
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthenticationEmail = AuthenticationEmail(FirebaseAuth.getInstance()),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val korisnikRepo = KorisnikRepoImplementation(firestore)

    private val _userState = MutableStateFlow<FirebaseUser?>(null)
    val userState: StateFlow<FirebaseUser?> = _userState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = authRepository.ulogujKorisnika(email, password)
            _loading.value = false

            result.onSuccess {
                _userState.value = it
                _errorMessage.value = null
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        ime : String,
        prezime : String,
        username: String,
        brojTelefona : String,
        urlFotografije : String
    ) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null

            val pogresnaSifra = bezbednaSifra(password)
            if (pogresnaSifra != null) {
                _errorMessage.value = pogresnaSifra
                _loading.value = false
                return@launch
            }

            if(!validanEmail(email)) {
                _errorMessage.value = "Unesite validan email!"
                _loading.value = false
                return@launch
            }

            val result = authRepository.napraviKorisnika(email, password)

            result.onSuccess { firebaseUser ->
                //Log.d("[AuthViewModel]", "Kreiran Firebase user: ${firebaseUser?.uid}")
                _userState.value = firebaseUser
                _errorMessage.value = null

                firebaseUser?.let { user ->
                    val noviKorisnik = Korisnik(
                        uid = user.uid,
                        username = username,
                        ime = ime,
                        prezime = prezime,
                        brojTelefona = brojTelefona,
                        urlFotografije = urlFotografije
                    )

                    //Log.d("[AuthViewModel]", "Pravim Firestore dokument za korisnika: $username")
                    val success = korisnikRepo.sacuvajKorisnika(noviKorisnik)
                    if(!success) {
                        _errorMessage.value = "Greska prilikom kreiranja korisnika!"
                        Log.e("[AuthViewModel]", "Neuspesno kreiranje Firestore dokumenta za: $username")
                    }
                }
            }.onFailure {
                _errorMessage.value = it.message
                //Log.e("[AuthViewModel]", "Greska prilikom kreiranja Firebase usera: ${it.message}")
            }

            _loading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.izlogujKorisnika()
            _userState.value = null
        }
    }

    fun checkCurrentUser() {
        _userState.value = authRepository.vratiTrenutnogKorisnika()
    }
}