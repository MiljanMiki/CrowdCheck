package com.lab.crowdcheck.data.remote.firebase


import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.util.Patterns
import kotlinx.coroutines.tasks.await



fun bezbednaSifra(sifra : String) : String?
{
    return when {
        sifra.length !in 6..30 -> "Sifra mora da ima min 6 a max 30 slova!"
        !sifra.any { it.isUpperCase() } -> "Sifra mora da ima barem jedno veliko slovo!"
        !sifra.any { it.isLowerCase() } -> "Sifra mora da ima barem jedno malo slovo!"
        !sifra.any { !it.isLetterOrDigit() } -> "Sifra mora da ima barem jedan simbol!"
        else -> null // null = valid
    }

}

fun validanEmail(email : String) : Boolean{
    return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}


class AuthenticationEmail(private val authentication : FirebaseAuth){

    companion object {
        private const val TAG_AUTH_EMAIL_AND_PASSWORD: String = "[AuthEmailPassword]"
    }

     suspend fun napraviKorisnika(email : String, password : String) : Result<FirebaseUser?>
    {
        return try {
            val greska : String? = bezbednaSifra(password)
            if(greska != null)
                throw Exception(greska)
            if(!validanEmail(email))
                throw Exception("Vas email nije validan!")


            val authResult: AuthResult =
                authentication.createUserWithEmailAndPassword(email, password)
                    .await()
            Log.d(TAG_AUTH_EMAIL_AND_PASSWORD, "napraviKorisnikaEmail:uspesno")
            Result.success(authResult.user)
        } catch (e: Exception) {
            Log.w(TAG_AUTH_EMAIL_AND_PASSWORD, "napraviKorisnikaEmail:neuspesno", e)
            Result.failure(e)
        }
    }
     suspend fun ulogujKorisnika(email : String, password : String) : Result<FirebaseUser?>
    {
        return try {
            val authResult: AuthResult = authentication
                .signInWithEmailAndPassword(email, password)
                .await()
            Log.d(TAG_AUTH_EMAIL_AND_PASSWORD, "ulogujKorisnikaEmail:uspesno")
            Result.success(authResult.user)
        } catch (e: Exception) {
            //ovo ce mozda 2 puta da mi stampa istu gresku...
            Log.w(TAG_AUTH_EMAIL_AND_PASSWORD, "ulogujKorisnikaEmail:neuspesno ", e)
            Result.failure(e)
        }
    }

    fun sendEmailPasswordReset(email : String)
    {
        authentication.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_AUTH_EMAIL_AND_PASSWORD, "Email sent.")
                }
            }
    }

    //uvek prvo proveri da li je korisnik ulogovan!!!
     suspend fun izbrisiKorisnika(){
        authentication.currentUser?.delete()?.await()
    }

     suspend fun izlogujKorisnika()
    {
        authentication.signOut()
    }
     fun vratiTrenutnogKorisnika() : FirebaseUser?
    {
        return authentication.currentUser
    }
     fun daLiJeKorisnikUlogovan() : Boolean
    {
        return authentication.currentUser != null
    }

}


