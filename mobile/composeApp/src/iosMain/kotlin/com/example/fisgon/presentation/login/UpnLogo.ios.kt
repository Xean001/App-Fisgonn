package com.example.fisgon.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import fisgon.composeapp.generated.resources.Res
import fisgon.composeapp.generated.resources.logo_upn
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun UpnLogo(modifier: Modifier, size: Dp) {
    Image(
        painter = painterResource(Res.drawable.logo_upn),
        contentDescription = "UPN",
        modifier = modifier.size(size)
    )
}
