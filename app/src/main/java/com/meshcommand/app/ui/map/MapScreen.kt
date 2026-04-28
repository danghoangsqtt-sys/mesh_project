package com.meshcommand.app.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import java.io.File

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

    AndroidView(
        factory = { ctx ->
            mapView.apply {
                getMapAsync { map ->
                    setupMap(map, ctx, soldiers, gatewayPos)
                }
            }
        },
        update = { view ->
            view.getMapAsync { map ->
                if (map.style != null) {
                    updateMarkers(map, soldiers, gatewayPos, viewModel)
                    drawTrails(map, trails)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )

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
    // Check for local MBTiles
    val mapsDir = File(context.filesDir, "maps")
    val mbtilesFile = mapsDir.listFiles()?.firstOrNull { it.extension == "mbtiles" }

    val styleUri = if (mbtilesFile != null) {
        // Use MBTiles source — would need a custom style referencing the mbtiles
        "asset://map_style_military.json"
    } else {
        "asset://map_style_military.json"
    }

    map.setStyle(Style.Builder().fromUri(styleUri)) { style ->
        // Add marker icons to style
        style.addImage("marker-ok", createCircleMarker(Color.parseColor("#4CAF50"), 24))
        style.addImage("marker-warn", createTriangleMarker(Color.parseColor("#FFC107"), 28))
        style.addImage("marker-crit", createDiamondMarker(Color.parseColor("#F44336"), 28))
        style.addImage("marker-offline", createCircleMarker(Color.parseColor("#9E9E9E"), 20))
        style.addImage("marker-gateway", createStarMarker(Color.parseColor("#2196F3"), 32))

        updateMarkers(map, soldiers, gatewayPos, null)
    }

    // Default camera — Vietnam center
    map.cameraPosition = CameraPosition.Builder()
        .target(LatLng(16.0, 108.0))
        .zoom(5.0)
        .build()

    // Enable gestures
    map.uiSettings.isZoomGesturesEnabled = true
    map.uiSettings.isScrollGesturesEnabled = true
    map.uiSettings.isRotateGesturesEnabled = true
    map.uiSettings.isTiltGesturesEnabled = true
}



private fun updateMarkers(
    map: MapLibreMap,
    soldiers: List<SoldierEntity>,
    gatewayPos: Pair<Double, Double>?,
    viewModel: MapViewModel?
) {
    val style = map.style ?: return

    // Remove old markers and re-add (acceptable at < 50 soldiers)
    map.annotations.forEach { map.removeAnnotation(it) }

    // Add soldier markers
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
            .snippet("HR:${soldier.heartRate} SpO2:${soldier.spo2} Bat:${"%.1f".format(soldier.batteryVolts)}V")

        val marker = map.addMarker(markerOptions)
    }

    // Add gateway marker
    gatewayPos?.let { (lat, lon) ->
        if (lat != 0.0 && lon != 0.0) {
            val markerOptions = org.maplibre.android.annotations.MarkerOptions()
                .position(LatLng(lat, lon))
                .title("Gateway")
                .snippet("Command Station")

            map.addMarker(markerOptions)
        }
    }

    // Marker click listener
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

// ─────────────────────────────────────────────
// Trail Polylines
// ─────────────────────────────────────────────

private val TRAIL_COLORS = intArrayOf(
    Color.parseColor("#4CAF50"), // Green
    Color.parseColor("#2196F3"), // Blue
    Color.parseColor("#FF9800"), // Orange
    Color.parseColor("#9C27B0"), // Purple
    Color.parseColor("#00BCD4"), // Cyan
    Color.parseColor("#E91E63"), // Pink
    Color.parseColor("#CDDC39"), // Lime
    Color.parseColor("#FF5722")  // Deep Orange
)

private fun drawTrails(
    map: MapLibreMap,
    trails: Map<Int, List<PositionHistoryEntity>>
) {
    // Remove existing polylines
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

// ─────────────────────────────────────────────
// Marker Icon Generators
// ─────────────────────────────────────────────

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
