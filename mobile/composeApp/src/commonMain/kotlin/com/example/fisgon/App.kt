package com.example.fisgon

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fisgon.data.repository.AuthRepositoryImpl
import com.example.fisgon.data.repository.ProductRepositoryImpl
import com.example.fisgon.domain.repository.AuthRepository
import com.example.fisgon.domain.repository.ProductRepository
import com.example.fisgon.domain.usecase.LoginUseCase
import com.example.fisgon.navigation.AppScreen
import com.example.fisgon.presentation.home.HomeScreen
import com.example.fisgon.presentation.home.HomeViewModel
import com.example.fisgon.presentation.login.LoginScreen
import com.example.fisgon.presentation.login.LoginViewModel
import com.example.fisgon.presentation.register.RegisterScreen
import com.example.fisgon.presentation.register.RegisterViewModel

@Composable
fun App(
    authRepository: AuthRepository = AuthRepositoryImpl(),
    productRepository: ProductRepository = ProductRepositoryImpl(),
    onOpenUrl: (String) -> Unit = {}
) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        var screen by remember { mutableStateOf<AppScreen>(AppScreen.Login) }

        when (val current = screen) {

            is AppScreen.Login -> {
                val loginVm: LoginViewModel = viewModel(key = "login") {
                    LoginViewModel(LoginUseCase(authRepository), authRepository)
                }
                LoginScreen(
                    viewModel = loginVm,
                    onLoginSuccess = { user, _ ->
                        screen = AppScreen.Home(user)
                    },
                    onNavigateToRegister = {
                        screen = AppScreen.Register
                    }
                )
            }

            is AppScreen.Register -> {
                val registerVm: RegisterViewModel = viewModel(key = "register") {
                    RegisterViewModel(authRepository)
                }
                RegisterScreen(
                    viewModel = registerVm,
                    onRegisterSuccess = { user, _ ->
                        screen = AppScreen.Home(user)
                    },
                    onBack = { screen = AppScreen.Login }
                )
            }

            is AppScreen.Home -> {
                val homeVm: HomeViewModel = viewModel(key = "home") {
                    HomeViewModel(productRepository, current.user)
                }
                HomeScreen(
                    viewModel = homeVm,
                    onOpenUrl = onOpenUrl
                )
            }
        }
    }
}
