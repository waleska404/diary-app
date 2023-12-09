package com.waleska404.moodtracker.presentation.screens.home

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.waleska404.moodtracker.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onMenuClicked: () -> Unit,
    navigateToWrite: () -> Unit,
    onSignOutClicked: () -> Unit,
) {
    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked,
        onDeleteAllClicked = { /*TODO*/ }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    onMenuClicked = onMenuClicked,
                    dateIsSelected = false,
                    onDateSelected = {}
                ) {

                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = navigateToWrite) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "New Diary Icon")
                }
            },
            content = {

            }
        )
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                content = {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo Image"
                    )
                    NavigationDrawerItem(
                        label = {
                            Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                                Image(
                                    painter = painterResource(id = R.drawable.google_logo),
                                    contentDescription = "Google Logo",
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sign Out",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        selected = false,
                        onClick = onSignOutClicked
                    )
                    NavigationDrawerItem(
                        label = {
                            Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete All Icon",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Delete All Diaries",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        selected = false,
                        onClick = onDeleteAllClicked
                    )
                }
            )
        },
        content = content
    )
}