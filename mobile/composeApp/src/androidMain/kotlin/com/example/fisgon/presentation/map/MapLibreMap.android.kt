package com.example.fisgon.presentation.map

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private val LIMA = LatLng(-12.0464, -77.0428)

@Composable
actual fun MapLibreMap(modifier: Modifier) {
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(
        factory = { _ ->
            mapView.apply {
                getMapAsync { map ->
                    map.setStyle(Style.Builder().fromUri(STYLE_URL)) {
                        map.cameraPosition = CameraPosition.Builder()
                            .target(LIMA)
                            .zoom(12.0)
                            .build()
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply { onCreate(Bundle()) }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    return mapView
}
