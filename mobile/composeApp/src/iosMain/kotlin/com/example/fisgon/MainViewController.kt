package com.example.fisgon

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    App(panicRepository = com.example.fisgon.data.repository.PanicRepositoryImpl())
}