package com.lab.crowdcheck.presentation.pages

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab.crowdcheck.presentation.viewmodel.AuthViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import java.io.File
import java.util.Objects


@Composable
fun RegisterPage(
    onRegisterSuccess : ()-> Unit,
    onLoginClick: ()-> Unit,
    authViewModel : AuthViewModel
)
{
    var email by remember { mutableStateOf(value = "") }
    var password by remember { mutableStateOf(value = "") }
    var ime by remember { mutableStateOf("") }
    var prezime by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val brojTelefona by remember { mutableStateOf("")}

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val user by authViewModel.userState.collectAsState()
    val error by authViewModel.errorMessage.collectAsState()
    val loading by authViewModel.loading.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                // The image URI is already set by the contract, no need to do anything here
                // if you used the URI provided to the launch call.
            }
        }
    )

    // Launcher for requesting camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                val uri = createImageUri(context)
                imageUri = uri
                cameraLauncher.launch(uri)
            } else {
                // Handle permission denial
                // You can show a toast or a snackbar here
            }
        }
    )

    LaunchedEffect(user)
    {
        if(user!=null)
            onRegisterSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "SignUp Page", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email, onValueChange =  { email = it },
            label = { Text(text = "Email") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = ime, onValueChange =  { ime = it },
            label = { Text(text = "Ime") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = prezime, onValueChange =  { prezime = it },
            label = { Text(text = "Prezime") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username, onValueChange =  { username = it },
            label = { Text(text = "Korisnicko ime") })
        Spacer(modifier = Modifier.height(8.dp))



        Spacer(modifier = Modifier.height(8.dp))

        error?.let {
            Text(text = it, color = androidx.compose.ui.graphics.Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { authViewModel.signUp(email, password, ime, prezime, username, brojTelefona,"url") },
            enabled = !loading
        ) {
            Text(text = if (loading) "Nalog se kreira..." else "Napravi nalog")
        }



        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onLoginClick) {
            Text(text = "Vec imate nalog? Login")
        }
    }

}

@Composable
fun ImagePickerCircle(imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            // Display the selected image
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Display placeholder text
            Text(
                text = "Odaberite/slikajte fotografiju",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Helper function to create a URI for the camera to save the image to.
// This is necessary for modern Android versions to avoid permission issues.
private fun createImageUri(context: Context): Uri {
    val timestamp = System.currentTimeMillis()
    val imageFileName = "JPEG_" + timestamp + "_"
    val storageDir : File? = context.externalCacheDir
    val image = File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",         /* suffix */
        storageDir      /* directory */
    )
    return androidx.core.content.FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", // Make sure this matches your FileProvider authority
        image
    )
}
