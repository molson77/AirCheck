package com.example.aircheck

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.aircheck.data.AqiSuccessResponse
import com.example.aircheck.data.ForecastData
import com.example.aircheck.ui.AqiDescriptionData
import com.example.aircheck.ui.Utils
import com.example.aircheck.ui.rememberMapViewWithLifecycle
import com.example.aircheck.ui.theme.AirCheckTheme
import com.example.aircheck.ui.viewmodels.AqiViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AqiActivity : ComponentActivity() {

    private val viewModel: AqiViewModel by viewModels()

    companion object {
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"

        fun getAqiActivityIntent(context: Context, lat: Double, lng: Double): Intent {
            return Intent(context, AqiActivity::class.java).apply {
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val latitude: Double = intent.extras?.getDouble(EXTRA_LAT) ?: 0.0
        val longitude: Double = intent.extras?.getDouble(EXTRA_LNG) ?: 0.0

        viewModel.getAqiData(latitude, longitude)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        setContent {
            AirCheckTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.aircheck_blue)
                ) {
                    AqiContent(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.reset()
    }
}

@Composable
fun AqiContent(
    viewModel: AqiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        Modifier.fillMaxSize()
    ) {
        if(uiState.loading) {
            AqiLoading(
                Modifier.fillMaxSize()
            )
        } else if (uiState.errorMessage != null) {
            uiState.errorMessage?.let {
                AqiError(
                    message = it,
                    Modifier.fillMaxSize()
                )
            }
        } else if(uiState.response != null) {
            uiState.response?.let {
                uiState.descriptionData?.let { it1 ->
                    AqiDetails(
                        response = it,
                        descriptionData = it1,
                        yesterdayForecastData = uiState.yesterdayForecastData,
                        yesterdayDescriptionData = uiState.yesterdayDescriptionData,
                        tomorrowForecastData = uiState.tomorrowForecastData,
                        tomorrowDescriptionData = uiState.tomorrowDescriptionData,
                        Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun AqiDetails(
    response: AqiSuccessResponse,
    descriptionData: AqiDescriptionData,
    yesterdayForecastData: ForecastData?,
    yesterdayDescriptionData: AqiDescriptionData?,
    tomorrowForecastData: ForecastData?,
    tomorrowDescriptionData: AqiDescriptionData?,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            StationInformation(
                name = response.data.city.name,
                lat = response.data.city.geo[0],
                lng = response.data.city.geo[1],
                Modifier.padding(horizontal = 32.dp, vertical = 26.dp)
            )

            AqiMap(
                stationLat = response.data.city.geo[0],
                stationLng = response.data.city.geo[1],
                userLat = null,
                userLng = null,
                modifier = Modifier
                    .fillMaxHeight(0.55F)
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 18.dp)
            )
        }
        AqiInformation(
            response = response,
            descriptionData,
            yesterdayForecastData,
            yesterdayDescriptionData,
            tomorrowForecastData,
            tomorrowDescriptionData,
            Modifier
                .padding(vertical = 16.dp)
        )
    }
}

@Composable
fun StationInformation(
    name: String,
    lat: Double,
    lng: Double,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        Text(
            "Data retrieved from nearby station:",
            color = colorResource(id = R.color.aircheck_white),
            fontSize = TextUnit(12.0F, TextUnitType.Sp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Light
        )
        Text(
            text = name,
            color = colorResource(id = R.color.aircheck_white),
            fontSize = TextUnit(20.0F, TextUnitType.Sp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
        )
        Text(text = "$lat\u00B0 N, $lng\u00B0 W",
            color = colorResource(id = R.color.aircheck_white),
            fontSize = TextUnit(14.0F, TextUnitType.Sp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun AqiMap(
    stationLat: Double,
    stationLng: Double,
    userLat: Double?,
    userLng: Double?,
    onLoad: ((MapView) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxSize()
        ) {
            val mapViewState = rememberMapViewWithLifecycle(
                stationLat,
                stationLng,
                userLat,
                userLng
            )

            AndroidView(
                { mapViewState },
                modifier
            ) { mapView -> onLoad?.invoke(mapView) }

            Box(modifier = Modifier
                .align(Alignment.TopEnd)
                .wrapContentWidth()
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    val annotatedString = buildAnnotatedString {
                        append("© ")
                        pushStringAnnotation(tag = "OpenStreetMap", annotation = "https://google.com/policy")
                        withStyle(style = SpanStyle(color = Color.Blue)) {
                            append("OpenStreetMap")
                        }
                        append(" contributors")
                        pop()
                    }
                    Text(
                        text = annotatedString,
                        color = Color.Black,
                        fontSize = TextUnit(10.0F, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AqiInformation(
    response: AqiSuccessResponse,
    descriptionData: AqiDescriptionData,
    yesterdayForecastData: ForecastData?,
    yesterdayDescriptionData: AqiDescriptionData?,
    tomorrowForecastData: ForecastData?,
    tomorrowDescriptionData: AqiDescriptionData?,
    modifier: Modifier = Modifier
) {

    val pagerState = rememberPagerState(pageCount = {
        3
    })

    LaunchedEffect(key1 = null) {
        pagerState.scrollToPage(1)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(vertical = 8.dp),
            pageSize = PageSize.Fixed(280.dp),
            pageSpacing = 4.dp,
            snapPosition = SnapPosition.Center
        ) { page ->
            // Our page content
            when(page) {
                0 -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AqiForecast(
                            label = "Yesterday's AQI:",
                            forecast = yesterdayForecastData,
                            descriptionData = yesterdayDescriptionData,
                            modifier = Modifier
                        )
                    }
                }
                1 -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CurrentAqi(
                            aqi = response.data.aqi,
                            descriptionData = descriptionData,
                            modifier = Modifier
                        )
                    }
                }
                2 -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AqiForecast(
                            label = "Tomorrow's AQI:",
                            forecast = tomorrowForecastData,
                            descriptionData = tomorrowDescriptionData,
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentAqi(
    aqi: String,
    descriptionData: AqiDescriptionData,
    modifier: Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorResource(id = descriptionData.colorId),
        modifier = modifier.size(235.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(10.dp)
        ) {
            Column(
                Modifier
                    .padding(8.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Today's AQI:",
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(18.0F, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(text = aqi,
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(90.0F, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(text = descriptionData.quality,
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(25.0F, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AqiForecast(
    label: String,
    forecast: ForecastData?,
    descriptionData: AqiDescriptionData?,
    modifier: Modifier
) {
    if (forecast != null &&
        descriptionData != null) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colorResource(id = descriptionData.colorId),
            modifier = modifier.size(235.dp),
            shadowElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(10.dp)
            ) {
                Column(
                    Modifier
                        .padding(8.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label,
                        color = colorResource(id = R.color.aircheck_white),
                        fontSize = TextUnit(18.0F, TextUnitType.Sp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "${forecast.min}",
                            color = colorResource(id = R.color.aircheck_white),
                            fontSize = TextUnit(22.0F, TextUnitType.Sp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "${forecast.avg}",
                            color = colorResource(id = R.color.aircheck_white),
                            fontSize = TextUnit(60.0F, TextUnitType.Sp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "${forecast.max}",
                            color = colorResource(id = R.color.aircheck_white),
                            fontSize = TextUnit(22.0F, TextUnitType.Sp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(text = descriptionData.quality,
                        color = colorResource(id = R.color.aircheck_white),
                        fontSize = TextUnit(25.0F, TextUnitType.Sp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        AqiForecastError(
            label = label,
            modifier = Modifier
        )
    }
}

@Composable
fun AqiForecastError(
    label: String,
    modifier: Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorResource(id = R.color.aqi_error),
        modifier = modifier.size(235.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(10.dp)
        ) {
            Column(
                Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = label,
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(18.0F, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "N/A",
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(50.0F, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Unavailable",
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(25.0F, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AqiError(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 26.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                color = colorResource(id = R.color.aircheck_white),
                fontSize = TextUnit(20.0F, TextUnitType.Sp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
            )
        }
        Box(
            Modifier.fillMaxHeight(0.85F),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(70.dp),
                painter = painterResource(id = R.drawable.error),
                contentDescription = "Error",
                tint = colorResource(id = R.color.aircheck_white)
            )
        }
    }
}

@Composable
fun AqiLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 26.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Loading AQI data...",
                color = colorResource(id = R.color.aircheck_white),
                fontSize = TextUnit(20.0F, TextUnitType.Sp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
            )
        }
        Box(
            Modifier.fillMaxHeight(0.85F),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(70.dp),
                color = colorResource(id = R.color.aircheck_white),
                strokeWidth = 5.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AqiDetailsPreview() {
    val exampleResponse = Utils.getExampleResponse()
    AirCheckTheme {
        Surface(
            color = colorResource(id = R.color.aircheck_blue)
        ) {
            val result = Utils.getExampleResponse()
            val descriptionData = Utils.getDescriptionDataForAqiScore(result.data.aqi)

            val today = LocalDate.now()
            val yesterday = today.minusDays(1).format(DateTimeFormatter.ofPattern(AqiViewModel.DATE_PATTERN))
            val tomorrow = today.plusDays(1).format(DateTimeFormatter.ofPattern(AqiViewModel.DATE_PATTERN))

            val yesterdayForecastData = result.data.forecast.daily.pm25.find { it.day == yesterday }
            val tomorrowForecastData = result.data.forecast.daily.pm25.find { it.day == tomorrow }

            AqiDetails(
                response = result,
                descriptionData = descriptionData,
                yesterdayForecastData = yesterdayForecastData,
                yesterdayDescriptionData = yesterdayForecastData?.let { Utils.getDescriptionDataForAqiScore(it.avg.toString()) },
                tomorrowForecastData = tomorrowForecastData,
                tomorrowDescriptionData = tomorrowForecastData?.let { Utils.getDescriptionDataForAqiScore(it.avg.toString()) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AqiLoadingPreview() {
    AirCheckTheme {
        Surface(
            color = colorResource(id = R.color.aircheck_blue)
        ) {
            AqiLoading(
                Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AqiErrorPreview() {
    AirCheckTheme {
        Surface(
            color = colorResource(id = R.color.aircheck_blue)
        ) {
            AqiError(
                "An error occurred, please try again.",
                Modifier.fillMaxSize()
            )
        }
    }
}