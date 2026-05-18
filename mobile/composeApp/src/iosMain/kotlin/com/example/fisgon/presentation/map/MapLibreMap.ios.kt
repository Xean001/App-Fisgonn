package com.example.fisgon.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

private fun buildMapHtml(markers: List<IncidentMarker>): String {
    val markersJson = markers.joinToString(",") { m ->
        """{"lat":${m.latitude},"lng":${m.longitude},"title":"${m.title.replace("\"", "\\\"")}"}"""
    }
    return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no"/>
<link href="https://unpkg.com/maplibre-gl@3.6.2/dist/maplibre-gl.css" rel="stylesheet"/>
<script src="https://unpkg.com/maplibre-gl@3.6.2/dist/maplibre-gl.js"></script>
<style>
  body { margin: 0; padding: 0; }
  #map { position: absolute; top: 0; bottom: 0; width: 100%; }
</style>
</head>
<body>
<div id="map"></div>
<script>
var map = new maplibregl.Map({
    container: 'map',
    style: 'https://tiles.openfreemap.org/styles/liberty',
    center: [-77.0428, -12.0464],
    zoom: 14
});
var userMarker = null;
var initialMarkers = [$markersJson];

function createCircle(center, radiusKm) {
    var ring = [];
    for (var i = 0; i <= 64; i++) {
        var angle = (i / 64) * 2 * Math.PI;
        var dx = radiusKm / (111.320 * Math.cos(center[1] * Math.PI / 180));
        var dy = radiusKm / 110.574;
        ring.push([center[0] + dx * Math.cos(angle), center[1] + dy * Math.sin(angle)]);
    }
    return { type: 'Feature', geometry: { type: 'Polygon', coordinates: [ring] } };
}

function addIncidentMarker(m, index) {
    var el = document.createElement('div');
    el.style.cssText = 'width:14px;height:14px;border-radius:50%;background:#FF4444;border:3px solid white;box-shadow:0 0 6px rgba(0,0,0,0.4)';
    new maplibregl.Marker({element: el}).setLngLat([m.lng, m.lat]).addTo(map);
    var sid = 'circle-' + index;
    map.addSource(sid, { type: 'geojson', data: createCircle([m.lng, m.lat], 5) });
    map.addLayer({ id: sid + '-fill', type: 'fill', source: sid,
        paint: { 'fill-color': '#FF4444', 'fill-opacity': 0.13 } });
    map.addLayer({ id: sid + '-line', type: 'line', source: sid,
        paint: { 'line-color': '#FF4444', 'line-width': 2, 'line-opacity': 0.7 } });
}

map.on('load', function() {
    initialMarkers.forEach(function(m, i) { addIncidentMarker(m, i); });
});

if (navigator.geolocation) {
    navigator.geolocation.watchPosition(function(pos) {
        var lng = pos.coords.longitude;
        var lat = pos.coords.latitude;
        if (!userMarker) {
            var el = document.createElement('div');
            el.style.cssText = 'width:16px;height:16px;border-radius:50%;background:#4A90E2;border:3px solid white;box-shadow:0 0 0 6px rgba(74,144,226,0.25)';
            userMarker = new maplibregl.Marker({element: el}).setLngLat([lng, lat]).addTo(map);
            map.flyTo({center: [lng, lat], zoom: 15});
        } else {
            userMarker.setLngLat([lng, lat]);
        }
    }, null, {enableHighAccuracy: true, timeout: 10000});
}
</script>
</body>
</html>
    """.trimIndent()
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapLibreMap(
    modifier: Modifier,
    latitude: Double?,
    longitude: Double?,
    markers: List<IncidentMarker>
) {
    key(markers.size) {
        UIKitView(
            factory = {
                val config = WKWebViewConfiguration()
                val webView = WKWebView(
                    frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                    configuration = config
                )
                webView.loadHTMLString(buildMapHtml(markers), baseURL = null)
                webView
            },
            modifier = modifier
        )
    }
}
