package com.waleska404.moodtracker.navigation

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import com.waleska404.moodtracker.model.Mood
import com.waleska404.moodtracker.model.RequestState
import com.waleska404.moodtracker.presentation.components.DisplayAlertDialog
import com.waleska404.moodtracker.presentation.screens.auth.AuthenticationScreen
import com.waleska404.moodtracker.presentation.screens.auth.AuthenticationViewModel
import com.waleska404.moodtracker.presentation.screens.home.HomeScreen
import com.waleska404.moodtracker.presentation.screens.home.HomeViewModel
import com.waleska404.moodtracker.presentation.screens.write.WriteScreen
import com.waleska404.moodtracker.presentation.screens.write.WriteViewModel
import com.waleska404.moodtracker.util.Constants.APP_ID
import com.waleska404.moodtracker.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit,
) {
    NavHost(
        startDestination = startDestination,
        navController = navController
    ) {
        authenticationRoute(
            navigateToHomeScreen = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            },
            onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToWriteScreen = {
                navController.navigate(Screen.Write.route)
            },
            navigateToAuthScreen = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            onDataLoaded = onDataLoaded,
            navigateToWriteScreenWithArgs = { diaryId ->
                navController.navigate(Screen.Write.passDiaryId(diaryId = diaryId))
            }
        )
        writeRoute(
            navigateBack = {
                navController.popBackStack()
            }
        )
    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHomeScreen: () -> Unit,
    onDataLoaded: () -> Unit,
) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val authenticated by viewModel.authenticated
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }

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
            navigateToHomeScreen = navigateToHomeScreen
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.homeRoute(
    navigateToWriteScreen: () -> Unit,
    navigateToWriteScreenWithArgs: (String) -> Unit,
    navigateToAuthScreen: () -> Unit,
    onDataLoaded: () -> Unit,
) {
    composable(route = Screen.Home.route) {
        val viewModel: HomeViewModel = viewModel()
        val diaries by viewModel.diaries
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch { drawerState.open() }
            },
            onSignOutClicked = { signOutDialogOpened = true },
            navigateToWrite = navigateToWriteScreen,
            navigateToWriteScreenWithArgs = navigateToWriteScreenWithArgs
        )
        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure yo want to log out from your Google account?",
            dialogOpened = signOutDialogOpened,
            onDialogClosed = { signOutDialogOpened = false },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
                            navigateToAuthScreen()
                        }
                    }
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(navigateBack: () -> Unit) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteViewModel = viewModel()
        val context = LocalContext.current
        val uiState = viewModel.uiState
        val galleryState = viewModel.galleryState
        val pagerState = rememberPagerState(pageCount = { Mood.values().size })
        val pageNumber by remember { derivedStateOf { pagerState.currentPage } }

        WriteScreen(
            uiState = uiState,
            pagerState = pagerState,
            galleryState = galleryState,
            moodName = { Mood.values()[pageNumber].name },
            onTitleChanged = { viewModel.setTitle(title = it) },
            onDescriptionChanged = { viewModel.setDescription(description = it) },
            onDeleteConfirmed = { viewModel.deleteDiary(
                onSuccess = {
                    Toast.makeText(
                        context,
                        "Deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateBack()
                },
                onError = { message ->
                    Toast.makeText(
                        context,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) },
            onDateTimeUpdated = { viewModel.updateDateTime(zonedDateTime = it) },
            onBackPressed = navigateBack,
            onSaveClicked = {
                viewModel.upsertDiary(
                    diary = it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = navigateBack,
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onImageSelect = {},
            onImageDeleteClicked = {}
        )
    }
}