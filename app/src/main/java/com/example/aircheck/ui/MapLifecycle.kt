package com.example.aircheck.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.aircheck.R
import com.example.aircheck.data.AqiRepository
import com.example.aircheck.data.AqiService
import com.example.aircheck.ui.viewmodels.AqiViewModel
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.TilesOverlay


@Composable
fun rememberMapViewWithLifecycle(
    stationLat: Double,
    stationLng: Double,
    userLat: Double?,
    userLng: Double?
): MapView {
    val context = LocalContext.current

    val provider = MapTileProviderBasic(
        context,
        object : OnlineTileSourceBase(
            "AQICN",
            5,
            18,
            256,
            "png?apikey=123",
            arrayOfNulls<String>(0)
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                return "https://tiles.aqicn.org/tiles/usepa-aqi/" +
                        "${MapTileIndex.getZoom(pMapTileIndex)}/" +
                        "${MapTileIndex.getX(pMapTileIndex)}/" +
                        "${MapTileIndex.getY(pMapTileIndex)}.png?" +
                        "token=${AqiRepository.TOKEN}"
            }
        })

    val mapView = remember {
        MapView(context).apply {

            // map options
            id = R.id.mapview
            minZoomLevel = 5.0
            maxZoomLevel = 12.0
            setZoomLevel(7.5)
            isHorizontalMapRepetitionEnabled = true
            isVerticalMapRepetitionEnabled = false
            setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            controller.setCenter(GeoPoint(stationLat, stationLng))

            // markers for station and potential user location
            val stationPoint = GeoPoint(stationLat, stationLng)
            val stationMarker = Marker(this)
            stationMarker.setPosition(stationPoint)
            stationMarker.setTextIcon("Station")
            stationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            overlays.add(stationMarker)

            if(userLat != null && userLng != null) {
                val userPoint = GeoPoint(userLat, userLng)
                val userMarker = Marker(this)
                userMarker.setPosition(userPoint)
                userMarker.setTextIcon("You")
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                overlays.add(userMarker)
            }

            overlays.add(
                TilesOverlay(provider, context).apply {
                    loadingBackgroundColor = Color.Transparent.toArgb()
                    loadingLineColor = Color.Transparent.toArgb()
                }
            )
            overlays.add(
                MinimapOverlay(context, this.tileRequestCompleteHandler).apply {
                    width = context.resources.displayMetrics.widthPixels / 6
                    height = context.resources.displayMetrics.heightPixels / 8
                }
            )

        }
    }

    // Makes MapView follow the lifecycle of this composable
    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
    }