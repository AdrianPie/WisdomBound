package com.lernhero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lernhero.auth.AuthScreen
import com.lernhero.auth.CharacterCreationScreen
import com.lernhero.home.navigation.HomeNavGraph
import com.lernhero.shared.navigation.Screen


@Composable
fun SetupNavGraph(startDestination: Screen = Screen.Auth) {

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        composable<Screen.Auth>{
            AuthScreen(
                navigateToHome = {
                    navController.navigate(Screen.HomeGraph){
                        popUpTo<Screen.Auth>{
                            inclusive = true
                        }
                    }
                },
                navigateToCharacterCreation = {
                    navController.navigate(Screen.CharacterCreation){
                        popUpTo<Screen.Auth>{
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<Screen.CharacterCreation>{
            CharacterCreationScreen(
                navigateToHome = {
                    navController.navigate(Screen.HomeGraph){
                        popUpTo<Screen.CharacterCreation>{
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<Screen.HomeGraph>{
            HomeNavGraph()
        }
    }
}
