/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen
import com.example.lunchtray.ui.theme.LunchTrayTheme

enum class LunchTrayScreen (@StringRes val title: Int) {
    StartOrder(title = R.string.app_name_short),
    EntreeMenu(title = R.string.choose_entree),
    AccompanimentMenu(title = R.string.choose_accompaniment),
    SideDishMenu(title = R.string.choose_side_dish),
    Checkout(title = R.string.order_checkout)
}

// TopAppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchAppBar(
    currentScreen: LunchTrayScreen,
    isBackButtonEnabled : Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(currentScreen.title),
                textAlign = TextAlign.Center
            ) },
        navigationIcon = {
            if(isBackButtonEnabled) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayApp() {
    // NavHostController
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Create ViewModel
    val viewModel: OrderViewModel = viewModel()

    val currentScreen = LunchTrayScreen.valueOf(
        backStackEntry?.destination?.route ?: LunchTrayScreen.StartOrder.name
    )

    Scaffold(
        topBar = {
            LunchAppBar(
                currentScreen = currentScreen,
                isBackButtonEnabled = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        // Navigation host
        NavHost(
            navController = navController,
            startDestination = LunchTrayScreen.StartOrder.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = LunchTrayScreen.StartOrder.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = {
                        viewModel.resetOrder()
                        navController.navigate(LunchTrayScreen.EntreeMenu.name)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = LunchTrayScreen.EntreeMenu.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onSelectionChanged = { viewModel.updateEntree(it) },
                    onNextButtonClicked = { navController.navigate(LunchTrayScreen.SideDishMenu.name) },
                    onCancelButtonClicked = { navController.navigate(LunchTrayScreen.StartOrder.name) }
                )
            }
            composable(route = LunchTrayScreen.SideDishMenu.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onSelectionChanged = { viewModel.updateSideDish(it) },
                    onNextButtonClicked = { navController.navigate(LunchTrayScreen.AccompanimentMenu.name) },
                    onCancelButtonClicked = { navController.navigate(LunchTrayScreen.StartOrder.name) }
                )
            }
            composable(route = LunchTrayScreen.AccompanimentMenu.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onSelectionChanged = { viewModel.updateAccompaniment(it) },
                    onNextButtonClicked = { navController.navigate(LunchTrayScreen.Checkout.name) },
                    onCancelButtonClicked = { navController.navigate(LunchTrayScreen.StartOrder.name) }
                )
            }
            composable(route = LunchTrayScreen.Checkout.name) {
                CheckoutScreen(
                    orderUiState = uiState,
                    onNextButtonClicked = { navController.navigate(LunchTrayScreen.StartOrder.name) },
                    onCancelButtonClicked = { navController.navigate(LunchTrayScreen.StartOrder.name) })
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LunchTrayAppPreview() {

    LunchTrayTheme {
        LunchTrayApp()
    }

}
