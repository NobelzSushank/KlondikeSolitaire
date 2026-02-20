package com.example.klondikesolitaire.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.klondikesolitaire.ads.AdsController
import com.example.klondikesolitaire.ui.navigation.AppNavGraph
import com.example.klondikesolitaire.viewmodel.AppViewModel

@Composable
fun AppRoot(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    AppNavGraph(
        appViewModel = appViewModel,
        navController = navController
    )
}