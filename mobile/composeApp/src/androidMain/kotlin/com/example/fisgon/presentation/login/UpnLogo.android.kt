package com.example.fisgon.presentation.login

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.caverock.androidsvg.SVG
import com.example.fisgon.R

@Composable
actual fun UpnLogo(modifier: Modifier, size: Dp) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }.coerceAtLeast(1)

    val imageBitmap = remember(sizePx) {
        val svg = SVG.getFromResource(context, R.raw.logo_upn)
        svg.setDocumentWidth(sizePx.toFloat())
        svg.setDocumentHeight(sizePx.toFloat())
        val picture = svg.renderToPicture()

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawPicture(picture)
        bitmap.asImageBitmap()
    }

    Image(
        bitmap = imageBitmap,
        contentDescription = "UPN",
        modifier = modifier.size(size)
    )
}
