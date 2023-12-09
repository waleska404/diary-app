package com.waleska404.moodtracker.navigation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import com.waleska404.moodtracker.presentation.screens.auth.AuthenticationScreen
import com.waleska404.moodtracker.presentation.screens.auth.AuthenticationViewModel
import com.waleska404.moodtracker.util.Constants
import com.waleska404.moodtracker.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
) {
    NavHost(
        startDestination = startDestination,
        navController = navController
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            }
        )
        homeRoute()
        writeRoute()
    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val authenticated by viewModel.authenticated
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()
        AuthenticationScreen(
            authenticated = authenticated,
            loadingState = loadingState,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            },
            onTokenIdReceived = { tokenId ->
                Log.d("MYTAG", "AUTH: $tokenId")
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Authentication successful!")
                        viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(Exception(it))
                        viewModel.setLoading(false)
                    }
                )
            },
            onDialogDismissed = { message ->
                Log.d("MYTAG", "AUTH: $message")
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHomeScreen = navigateToHome
        )
    }
}

fun NavGraphBuilder.homeRoute() {
    composable(route = Screen.Home.route) {
        val scope = rememberCoroutineScope()
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        App.create(Constants.APP_ID).currentUser?.logOut()
                    }
                }) {
                Text(text = "LOGOUT")
            }
        }
    }
}

fun NavGraphBuilder.writeRoute() {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {

    }
}