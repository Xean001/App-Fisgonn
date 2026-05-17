package com.example.fisgon

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fisgon.data.repository.AuthRepositoryImpl
import com.example.fisgon.data.repository.NoopPanicRepository
import com.example.fisgon.data.repository.ProductRepositoryImpl
import com.example.fisgon.domain.repository.AuthRepository
import com.example.fisgon.domain.repository.LocationRepository
import com.example.fisgon.domain.repository.PanicRepository
import com.example.fisgon.domain.repository.ProductRepository
import com.example.fisgon.domain.usecase.LoginUseCase
import com.example.fisgon.navigation.AppScreen
import com.example.fisgon.presentation.home.HomeScreen
import com.example.fisgon.presentation.home.HomeViewModel
import com.example.fisgon.presentation.login.LoginScreen
import com.example.fisgon.presentation.login.LoginViewModel
import com.example.fisgon.presentation.panic.PanicViewModel
import com.example.fisgon.presentation.permissions.NoopPermissionsController
import com.example.fisgon.presentation.permissions.PermissionsController
import com.example.fisgon.presentation.permissions.PermissionsScreen
import com.example.fisgon.presentation.register.RegisterScreen
import com.example.fisgon.presentation.register.RegisterViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private object NoopLocationRepository : LocationRepository {
    override val location: StateFlow<com.example.fisgon.domain.entity.LocationData?> =
        MutableStateFlow(null)
    override fun startTracking() {}
    override fun stopTracking() {}
}

@Composable
fun App(
    authRepository: AuthRepository = AuthRepositoryImpl(),
    productRepository: ProductRepository = ProductRepositoryImpl(),
    permissionsController: PermissionsController = NoopPermissionsController,
    locationRepository: LocationRepository = NoopLocationRepository,
    panicRepository: PanicRepository = NoopPanicRepository(),
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
                    onLoginSuccess = { user, token ->
                        screen = AppScreen.Permissions(user, token)
                    },
                    onNavigateToRegister = { screen = AppScreen.Register }
                )
            }

            is AppScreen.Register -> {
                val registerVm: RegisterViewModel = viewModel(key = "register") {
                    RegisterViewModel(authRepository)
                }
                RegisterScreen(
                    viewModel = registerVm,
                    onRegisterSuccess = { user, token ->
                        screen = AppScreen.Permissions(user, token)
                    },
                    onBack = { screen = AppScreen.Login }
                )
            }

            is AppScreen.Permissions -> {
                PermissionsScreen(
                    user = current.user,
                    controller = permissionsController,
                    onContinue = { screen = AppScreen.Home(current.user, current.token) }
                )
            }

            is AppScreen.Home -> {
                val panicVm: PanicViewModel = viewModel(key = "panic") {
                    PanicViewModel(
                        panicRepository = panicRepository,
                        locationRepository = locationRepository,
                        token = current.token
                    )
                }
                val homeVm: HomeViewModel = viewModel(key = "home") {
                    HomeViewModel(productRepository, current.user)
                }
                HomeScreen(
                    viewModel = homeVm,
                    panicViewModel = panicVm,
                    onOpenUrl = onOpenUrl
                )
            }
        }
    }
}
