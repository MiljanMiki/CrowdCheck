package com.lab.crowdcheck.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.lab.crowdcheck.presentation.pages.LoginPage
import com.lab.crowdcheck.presentation.pages.RegisterPage
import com.lab.crowdcheck.presentation.viewmodel.AuthViewModel
import com.lab.crowdcheck.presentation.Routes
import com.lab.crowdcheck.presentation.pages.MainPage


@Composable
fun NavigationHost(
    firestore : FirebaseFirestore,
    authViewModel : AuthViewModel,
    modifier : Modifier
){
    val navController = rememberNavController()



    NavHost(
        navController,
        modifier = modifier,
        startDestination = Routes.loginScreen
    ){
        composable(route = Routes.loginScreen){
            LoginPage(
                onRegisterClick = {navController.navigate(route = Routes.registerScreen)},
                onLoginSucces = {navController.navigate(
                    route = Routes.mainScreen,
                    navOptions = navOptions { popUpTo(Routes.loginScreen){inclusive = true} }
                )
                },
                authViewModel)
        }

        composable(route = Routes.registerScreen){
            RegisterPage(
                onRegisterSuccess = { navController
                    .navigate(
                        route = Routes.mainScreen,
                        navOptions = navOptions { popUpTo(Routes.loginScreen){inclusive = true} }
                    )
                },
                onLoginClick = {navController.navigate(route = Routes.loginScreen)},
                authViewModel
            )
        }

        composable(route = Routes.mainScreen){
            MainPage(authViewModel)
        }
    }



}