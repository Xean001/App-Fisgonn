package com.example.fisgon.presentation.map

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import kotlin.math.cos
import kotlin.math.sin

private const val STYLE_URL    = "https://tiles.openfreemap.org/styles/liberty"
private val DEFAULT_LOCATION   = LatLng(-12.0464, -77.0428)
private const val MARKER_SRC   = "incident-markers"
private const val CIRCLE_SRC   = "incident-circles"

@SuppressLint("MissingPermission")
@Composable
actual fun MapLibreMap(
    modifier: Modifier,
    latitude: Double?,
    longitude: Double?,
    markers: List<IncidentMarker>
) {
    val context  = LocalContext.current
    val mapView  = rememberMapViewWithLifecycle()
    val mapRef   = remember { mutableStateOf<MapLibreMap?>(null) }
    val styleRef = remember { mutableStateOf<Style?>(null) }

    LaunchedEffect(latitude, longitude) {
        val map = mapRef.value ?: return@LaunchedEffect
        if (latitude != null && longitude != null) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15.0)
            )
        }
    }

    LaunchedEffect(markers) {
        styleRef.value?.let { updateMarkers(it, markers) }
    }

    AndroidView(
        factory = { _ ->
            mapView.apply {
                getMapAsync { map ->
                    mapRef.value = map
                    map.setStyle(Style.Builder().fromUri(STYLE_URL)) { style ->
                        styleRef.value = style

                        val target = if (latitude != null && longitude != null)
                            LatLng(latitude, longitude) else DEFAULT_LOCATION
                        map.cameraPosition = CameraPosition.Builder()
                            .target(target).zoom(15.0).build()

                        val lc = map.locationComponent
                        lc.activateLocationComponent(
                            LocationComponentActivationOptions.builder(context, style)
                                .useDefaultLocationEngine(true).build()
                        )
                        lc.isLocationComponentEnabled = true
                        lc.cameraMode = CameraMode.NONE
                        lc.renderMode = RenderMode.COMPASS

                        initSources(style)
                        updateMarkers(style, markers)
                    }
                }
            }
        },
        modifier = modifier
    )
}

private fun initSources(style: Style) {
    if (style.getSource(CIRCLE_SRC) == null) {
        style.addSource(GeoJsonSource(CIRCLE_SRC, FeatureCollection.fromFeatures(emptyList<Feature>())))
        style.addLayerBelow(
            FillLayer("circle-fill", CIRCLE_SRC).withProperties(
                fillColor("#FF4444"), fillOpacity(0.13f)
            ), "road-label"
        )
        style.addLayerBelow(
            LineLayer("circle-line", CIRCLE_SRC).withProperties(
                lineColor("#FF4444"), lineWidth(2f), lineOpacity(0.7f)
            ), "road-label"
        )
    }
    if (style.getSource(MARKER_SRC) == null) {
        style.addSource(GeoJsonSource(MARKER_SRC, FeatureCollection.fromFeatures(emptyList<Feature>())))
        style.addLayer(
            CircleLayer("marker-dot", MARKER_SRC).withProperties(
                circleRadius(10f),
                circleColor("#FF4444"),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(2.5f)
            )
        )
    }
}

private fun updateMarkers(style: Style, markers: List<IncidentMarker>) {
    val pts = markers.map { Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude)) }
    (style.getSource(MARKER_SRC) as? GeoJsonSource)
        ?.setGeoJson(FeatureCollection.fromFeatures(pts))

    val circles = markers.map { Feature.fromGeometry(circlePolygon(it.latitude, it.longitude, 5.0)) }
    (style.getSource(CIRCLE_SRC) as? GeoJsonSource)
        ?.setGeoJson(FeatureCollection.fromFeatures(circles))
}

private fun circlePolygon(lat: Double, lng: Double, radiusKm: Double): Polygon {
    val points = (0..64).map { i ->
        val a = Math.toRadians(i * 360.0 / 64)
        Point.fromLngLat(
            lng + radiusKm / (111.0 * cos(Math.toRadians(lat))) * sin(a),
            lat + radiusKm / 111.0 * cos(a)
        )
    }
    return Polygon.fromLngLats(listOf(points))
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context).apply { onCreate(Bundle()) } }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START   -> mapView.onStart()
                Lifecycle.Event.ON_RESUME  -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                Lifecycle.Event.ON_STOP    -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return mapView
}
