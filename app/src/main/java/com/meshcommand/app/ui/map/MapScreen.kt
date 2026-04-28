package com.meshcommand.app.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.meshcommand.app.data.entity.PositionHistoryEntity
import com.meshcommand.app.data.entity.SoldierEntity
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.HeatmapLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.RasterDemSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import com.google.gson.JsonObject
import java.io.File
import java.util.Locale
import org.json.JSONArray

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soldiers by viewModel.soldierPositions.collectAsState()
    val gatewayPos by viewModel.gatewayPosition.collectAsState()
    val cameraTarget by viewModel.cameraTarget.collectAsState()
    val trails by viewModel.allTrails.collectAsState()

    val savedGeofences by viewModel.savedGeofences.collectAsState()
    val isDrawing by viewModel.isDrawingMode.collectAsState()
    val drawPoints by viewModel.currentDrawPoints.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    // Initialize MapLibre once
    remember { MapLibre.getInstance(context) }

    val mapView = remember {
        MapView(context)
    }

    // Camera target changes
    LaunchedEffect(cameraTarget) {
        cameraTarget?.let { (lat, lon) ->
            mapView.getMapAsync { map ->
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16.0),
                    1000
                )
            }
            viewModel.clearCameraTarget()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                mapView.apply {
                    getMapAsync { map ->
                        setupMap(map, ctx, soldiers, gatewayPos)
                        map.addOnMapClickListener { latLng ->
                            viewModel.onMapClick(latLng.latitude, latLng.longitude)
                            true
                        }
                    }
                }
            },
            update = { view ->
                view.getMapAsync { map ->
                    if (map.style != null && map.style!!.isFullyLoaded) {
                        updateMarkers(map, soldiers, gatewayPos, viewModel)
                        updateGeoJsonSources(map, soldiers)
                        drawTrails(map, trails)
                        drawGeofences(map, savedGeofences, drawPoints)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Download Progress Overlay
        if (downloadProgress != null) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(androidx.compose.ui.graphics.Color(0xCC000000), shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Downloading Map: ${downloadProgress}%",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }

        // Overlay UI for Geofencing
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isDrawing) {
                Button(onClick = { viewModel.toggleDrawingMode() }) {
                    Text("Draw Geofence")
                }
                Button(
                    onClick = {
                        mapView.getMapAsync { map ->
                            val bounds = map.projection.visibleRegion.latLngBounds
                            val minZoom = map.cameraPosition.zoom
                            val maxZoom = minZoom + 3.0 // 3 extra levels for detail
                            val pixelRatio = context.resources.displayMetrics.density
                            val definition = org.maplibre.android.offline.OfflineTilePyramidRegionDefinition(
                                map.style?.uri ?: "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json",
                                bounds, minZoom, maxZoom, pixelRatio
                            )
                            val metadata = "{\"regionName\":\"Tactical Region\"}".toByteArray()

                            viewModel.setDownloadProgress(0)
                            org.maplibre.android.offline.OfflineManager.getInstance(context)
                                .createOfflineRegion(
                                    definition,
                                    metadata,
                                    object : org.maplibre.android.offline.OfflineManager.CreateOfflineRegionCallback {
                                        override fun onCreate(offlineRegion: org.maplibre.android.offline.OfflineRegion) {
                                            offlineRegion.setDownloadState(org.maplibre.android.offline.OfflineRegion.STATE_ACTIVE)
                                            offlineRegion.setObserver(object : org.maplibre.android.offline.OfflineRegion.OfflineRegionObserver {
                                                override fun onStatusChanged(status: org.maplibre.android.offline.OfflineRegionStatus) {
                                                    val percentage = if (status.requiredResourceCount >= 0L) {
                                                        (100.0 * status.completedResourceCount / status.requiredResourceCount).toInt()
                                                    } else {
                                                        0
                                                    }
                                                    viewModel.setDownloadProgress(percentage)
                                                    if (status.isComplete) {
                                                        viewModel.setDownloadProgress(null)
                                                        android.widget.Toast.makeText(context, "Offline Map Saved!", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                                override fun onError(error: org.maplibre.android.offline.OfflineRegionError) {
                                                    viewModel.setDownloadProgress(null)
                                                    android.widget.Toast.makeText(context, "Map Download Error: ${error.reason}", android.widget.Toast.LENGTH_SHORT).show()
                                                }

                                                override fun mapboxTileCountLimitExceeded(limit: Long) {
                                                    viewModel.setDownloadProgress(null)
                                                }
                                            })
                                        }

                                        override fun onError(error: String) {
                                            viewModel.setDownloadProgress(null)
                                        }
                                    }
                                )
                        }
                    },
                    enabled = downloadProgress == null
                ) {
                    Text("Download Map")
                }
            } else {
                Button(
                    onClick = { viewModel.toggleDrawingMode() },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Gray)
                ) {
                    Text("Cancel")
                }
                
                if (drawPoints.size >= 3) {
                    Button(
                        onClick = { viewModel.saveGeofence("Safe Zone", com.meshcommand.app.data.entity.GeofenceEntity.TYPE_SAFE) },
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                    ) {
                        Text("Save Safe")
                    }
                    Button(
                        onClick = { viewModel.saveGeofence("Danger Zone", com.meshcommand.app.data.entity.GeofenceEntity.TYPE_RESTRICTED) },
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF44336))
                    ) {
                        Text("Save Danger")
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
}

private fun setupMap(
    map: MapLibreMap,
    context: android.content.Context,
    soldiers: List<SoldierEntity>,
    gatewayPos: Pair<Double, Double>?
) {
    val mapsDir = File(context.filesDir, "maps")
    val mbtilesFile = mapsDir.listFiles()?.firstOrNull { it.extension == "mbtiles" }

    val styleUri = if (mbtilesFile != null) {
        "asset://map_style_military.json"
    } else {
        "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"
    }

    map.setStyle(Style.Builder().fromUri(styleUri)) { style ->
        // Add marker icons to style
        style.addImage("marker-ok", createCircleMarker(Color.parseColor("#4CAF50"), 24))
        style.addImage("marker-warn", createTriangleMarker(Color.parseColor("#FFC107"), 28))
        style.addImage("marker-crit", createDiamondMarker(Color.parseColor("#F44336"), 28))
        style.addImage("marker-offline", createCircleMarker(Color.parseColor("#9E9E9E"), 20))
        style.addImage("marker-gateway", createStarMarker(Color.parseColor("#2196F3"), 32))

        // Phase 5.3: 3D Terrain (Disabled due to 404 dead link)
        // style.addSource(RasterDemSource("terrain-source", "https://s3.amazonaws.com/elevation-tiles-prod/terrarium/{z}/{x}/{y}.png"))
        
        setupAdvancedLayers(style)

        updateMarkers(map, soldiers, gatewayPos, null)
        updateGeoJsonSources(map, soldiers)
    }

    map.cameraPosition = CameraPosition.Builder()
        .target(LatLng(12.2388, 109.1967))
        .zoom(14.0)
        .tilt(45.0) // Phase 5.3: Pitch for 3D
        .build()

    // Phase 5.4: Compass Widget
    map.uiSettings.isCompassEnabled = true
    map.uiSettings.isZoomGesturesEnabled = true
    map.uiSettings.isScrollGesturesEnabled = true
    map.uiSettings.isRotateGesturesEnabled = true
    map.uiSettings.isTiltGesturesEnabled = true
}

private fun setupAdvancedLayers(style: Style) {
    // 1. Accuracy Circles Source & Layer (Phase 5.5)
    style.addSource(GeoJsonSource("accuracy-source"))
    val accuracyLayer = CircleLayer("accuracy-layer", "accuracy-source").withProperties(
        circleRadius(20f),
        circleColor(Color.parseColor("#4CAF50")),
        circleOpacity(0.2f),
        circleStrokeColor(Color.parseColor("#4CAF50")),
        circleStrokeWidth(1f)
    )
    style.addLayer(accuracyLayer)

    // 2. Heatmap Source & Layer (Phase 5.1)
    style.addSource(GeoJsonSource("heatmap-source"))
    val heatmapLayer = HeatmapLayer("heatmap-layer", "heatmap-source").withProperties(
        heatmapWeight(1.0f),
        heatmapIntensity(1.0f),
        heatmapRadius(30f),
        heatmapOpacity(0.6f)
    )
    style.addLayerAbove(heatmapLayer, "accuracy-layer")

    // 3. Clustering Source & Layer (Phase 5.2)
    style.addSource(GeoJsonSource("cluster-source", GeoJsonOptions().withCluster(true).withClusterRadius(50)))
    val clusterLayer = CircleLayer("cluster-layer", "cluster-source").withProperties(
        circleRadius(15f),
        circleColor(Color.parseColor("#FF9800")),
        circleOpacity(0.8f)
    ).withFilter(org.maplibre.android.style.expressions.Expression.has("point_count"))
    
    val clusterCountLayer = SymbolLayer("cluster-count-layer", "cluster-source").withProperties(
        textField(org.maplibre.android.style.expressions.Expression.toString(org.maplibre.android.style.expressions.Expression.get("point_count"))),
        textSize(12f),
        textColor(Color.WHITE)
    )
    
    style.addLayerAbove(clusterLayer, "heatmap-layer")
    style.addLayerAbove(clusterCountLayer, "cluster-layer")
}

private fun updateGeoJsonSources(map: MapLibreMap, soldiers: List<SoldierEntity>) {
    val style = map.style ?: return
    if (!style.isFullyLoaded) return
    
    val features = soldiers.filter { it.latitude != 0.0 && it.longitude != 0.0 }.map { soldier ->
        val feature = Feature.fromGeometry(Point.fromLngLat(soldier.longitude, soldier.latitude))
        feature.addNumberProperty("nodeId", soldier.nodeId)
        feature.addNumberProperty("hr", soldier.heartRate)
        feature
    }
    val featureCollection = FeatureCollection.fromFeatures(features)

    (style.getSource("accuracy-source") as? GeoJsonSource)?.setGeoJson(featureCollection)
    (style.getSource("heatmap-source") as? GeoJsonSource)?.setGeoJson(featureCollection)
    (style.getSource("cluster-source") as? GeoJsonSource)?.setGeoJson(featureCollection)
}

private fun updateMarkers(
    map: MapLibreMap,
    soldiers: List<SoldierEntity>,
    gatewayPos: Pair<Double, Double>?,
    viewModel: MapViewModel?
) {
    val style = map.style ?: return

    map.annotations.forEach { map.removeAnnotation(it) }

    for (soldier in soldiers) {
        if (soldier.latitude == 0.0 && soldier.longitude == 0.0) continue

        val iconId = when {
            !soldier.isOnline -> "marker-offline"
            soldier.alertLevel >= 2 -> "marker-crit"
            soldier.alertLevel == 1 -> "marker-warn"
            else -> "marker-ok"
        }

        val icon = style.getImage(iconId) ?: continue
        val markerOptions = org.maplibre.android.annotations.MarkerOptions()
            .position(LatLng(soldier.latitude, soldier.longitude))
            .title("Node ${soldier.nodeId}")
            .snippet("HR:${soldier.heartRate} SpO2:${soldier.spo2} Bat:${String.format(Locale.US, "%.1f", soldier.batteryVolts)}V")

        map.addMarker(markerOptions)
    }

    gatewayPos?.let { (lat, lon) ->
        if (lat != 0.0 && lon != 0.0) {
            val markerOptions = org.maplibre.android.annotations.MarkerOptions()
                .position(LatLng(lat, lon))
                .title("Gateway")
                .snippet("Command Station")

            map.addMarker(markerOptions)
        }
    }

    viewModel?.let { vm ->
        map.setOnMarkerClickListener { marker ->
            val nodeIdStr = marker.title.removePrefix("Node ")
            nodeIdStr.toIntOrNull()?.let { nodeId ->
                vm.selectSoldier(nodeId)
            }
            true
        }
    }
}

private val TRAIL_COLORS = intArrayOf(
    Color.parseColor("#4CAF50"),
    Color.parseColor("#2196F3"),
    Color.parseColor("#FF9800"),
    Color.parseColor("#9C27B0"),
    Color.parseColor("#00BCD4"),
    Color.parseColor("#E91E63"),
    Color.parseColor("#CDDC39"),
    Color.parseColor("#FF5722")
)

private fun drawTrails(
    map: MapLibreMap,
    trails: Map<Int, List<PositionHistoryEntity>>
) {
    map.polylines.forEach { map.removePolyline(it) }

    for ((nodeId, positions) in trails) {
        if (positions.size < 2) continue

        val points = positions.map { LatLng(it.latitude, it.longitude) }
        val colorIndex = nodeId % TRAIL_COLORS.size
        val color = TRAIL_COLORS[colorIndex]

        val polylineOptions = org.maplibre.android.annotations.PolylineOptions()
            .addAll(points)
            .color(color)
            .width(3f)

        map.addPolyline(polylineOptions)
    }
}

private fun drawGeofences(
    map: MapLibreMap,
    savedGeofences: List<com.meshcommand.app.data.entity.GeofenceEntity>,
    currentDrawPoints: List<Pair<Double, Double>>
) {
    map.polygons.forEach { map.removePolygon(it) }

    // Draw saved geofences
    for (geofence in savedGeofences) {
        try {
            val jsonArray = JSONArray(geofence.pointsJson)
            val points = mutableListOf<LatLng>()
            for (i in 0 until jsonArray.length()) {
                val pt = jsonArray.getJSONArray(i)
                points.add(LatLng(pt.getDouble(0), pt.getDouble(1)))
            }
            if (points.size >= 3) {
                val color = if (geofence.zoneType == com.meshcommand.app.data.entity.GeofenceEntity.TYPE_RESTRICTED) {
                    Color.argb(100, 244, 67, 54) // Translucent red
                } else {
                    Color.argb(100, 76, 175, 80) // Translucent green
                }
                
                val polyOptions = org.maplibre.android.annotations.PolygonOptions()
                    .addAll(points)
                    .fillColor(color)
                map.addPolygon(polyOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Draw current drawing points
    if (currentDrawPoints.size >= 3) {
        val points = currentDrawPoints.map { LatLng(it.first, it.second) }
        val polyOptions = org.maplibre.android.annotations.PolygonOptions()
            .addAll(points)
            .fillColor(Color.argb(120, 255, 193, 7)) // Yellow draft
        map.addPolygon(polyOptions)
    } else if (currentDrawPoints.isNotEmpty()) {
        // Draw draft lines if less than 3 points
        val points = currentDrawPoints.map { LatLng(it.first, it.second) }
        val polylineOptions = org.maplibre.android.annotations.PolylineOptions()
            .addAll(points)
            .color(Color.YELLOW)
            .width(3f)
        map.addPolyline(polylineOptions)
    }
}

private fun createCircleMarker(color: Int, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val radius = size / 2f
    canvas.drawCircle(radius, radius, radius - 2, paint)
    canvas.drawCircle(radius, radius, radius - 2, border)
    return bitmap
}

private fun createTriangleMarker(color: Int, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val path = Path().apply {
        moveTo(size / 2f, 2f)
        lineTo(size - 2f, size - 2f)
        lineTo(2f, size - 2f)
        close()
    }
    canvas.drawPath(path, paint)
    canvas.drawPath(path, border)
    return bitmap
}

private fun createDiamondMarker(color: Int, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val half = size / 2f
    val path = Path().apply {
        moveTo(half, 2f)
        lineTo(size - 2f, half)
        lineTo(half, size - 2f)
        lineTo(2f, half)
        close()
    }
    canvas.drawPath(path, paint)
    canvas.drawPath(path, border)
    return bitmap
}

private fun createStarMarker(color: Int, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val half = size / 2f
    val outerR = half - 2
    val innerR = outerR * 0.4f
    val path = Path()
    for (i in 0 until 5) {
        val outerAngle = Math.toRadians((i * 72 - 90).toDouble())
        val innerAngle = Math.toRadians((i * 72 + 36 - 90).toDouble())
        val ox = half + outerR * Math.cos(outerAngle).toFloat()
        val oy = half + outerR * Math.sin(outerAngle).toFloat()
        val ix = half + innerR * Math.cos(innerAngle).toFloat()
        val iy = half + innerR * Math.sin(innerAngle).toFloat()
        if (i == 0) path.moveTo(ox, oy) else path.lineTo(ox, oy)
        path.lineTo(ix, iy)
    }
    path.close()
    canvas.drawPath(path, paint)
    canvas.drawPath(path, border)
    return bitmap
}
