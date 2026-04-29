package com.example.fisgon

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fisgon.data.db.FisgonDatabaseHelper
import com.example.fisgon.data.repository.AndroidAuthRepositoryImpl
import com.example.fisgon.data.repository.AndroidProductRepositoryImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val dbHelper        = FisgonDatabaseHelper(this)
        val authRepository  = AndroidAuthRepositoryImpl(dbHelper)
        val productRepository = AndroidProductRepositoryImpl(dbHelper)

        setContent {
            App(
                authRepository    = authRepository,
                productRepository = productRepository,
                onOpenUrl         = { url ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        }
    }
}
