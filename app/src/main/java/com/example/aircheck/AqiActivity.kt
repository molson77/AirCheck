package com.example.aircheck

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.aircheck.data.AqiSuccessResponse
import com.example.aircheck.data.ForecastData
import com.example.aircheck.ui.AqiDescriptionData
import com.example.aircheck.ui.Utils
import com.example.aircheck.ui.theme.AirCheckTheme
import com.example.aircheck.ui.viewmodels.AqiViewModel
import dagger.hilt.android.AndroidEntryPoint
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

        setContent {
            AirCheckTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.aircheck_blue)
                ) {
                    AqiContent(viewModel = viewModel)
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
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val density = LocalDensity.current

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically {
                // Slide in from 40 dp from the top.
                with(density) { -40.dp.roundToPx() }
            } + expandVertically(
                // Expand from the top.
                expandFrom = Alignment.Top
            ) + fadeIn(
                // Fade in with the initial alpha of 0.3f.
                initialAlpha = 0.3f
            ),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            StationInformation(
                name = response.data.city.name,
                lat = response.data.city.geo[0],
                lng = response.data.city.geo[1],
                Modifier.padding(horizontal = 32.dp, vertical = 26.dp)
            )
        }

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically {
                // Slide in from 40 dp from the top.
                with(density) { -40.dp.roundToPx() }
            } + expandVertically(
                // Expand from the top.
                expandFrom = Alignment.Top
            ) + fadeIn(
                // Fade in with the initial alpha of 0.3f.
                initialAlpha = 0.3f
            ),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            AqiInformation(
                response = response,
                descriptionData,
                yesterdayForecastData,
                yesterdayDescriptionData,
                tomorrowForecastData,
                tomorrowDescriptionData,
                Modifier.padding(vertical = 14.dp)
            )
        }

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
fun AqiInformation(
    response: AqiSuccessResponse,
    descriptionData: AqiDescriptionData,
    yesterdayForecastData: ForecastData?,
    yesterdayDescriptionData: AqiDescriptionData?,
    tomorrowForecastData: ForecastData?,
    tomorrowDescriptionData: AqiDescriptionData?,
    modifier: Modifier = Modifier
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.padding(horizontal = 60.dp)
    ) {

        if (yesterdayForecastData != null &&
            yesterdayDescriptionData != null) {
            AqiForecast(
                label = "Yesterday's AQI:",
                forecast = yesterdayForecastData,
                descriptionData = yesterdayDescriptionData,
                modifier = Modifier.padding()
            )
        }

        CurrentAqi(aqi = response.data.aqi, descriptionData = descriptionData)

        if (tomorrowForecastData != null &&
            tomorrowDescriptionData != null) {
            AqiForecast(
                label = "Tomorrow's AQI:",
                forecast = tomorrowForecastData,
                descriptionData = tomorrowDescriptionData,
                modifier = Modifier.padding()
            )
        }
    }
}

@Composable
fun CurrentAqi(
    aqi: Int,
    descriptionData: AqiDescriptionData
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorResource(id = descriptionData.colorId),
        modifier = Modifier.fillMaxWidth(),
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
                Text(text = "Today's AQI:",
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(18.0F, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "$aqi",
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(100.0F, TextUnitType.Sp),
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
    forecast: ForecastData,
    descriptionData: AqiDescriptionData,
    modifier: Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorResource(id = descriptionData.colorId),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = label,
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(16.0F, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "${forecast.min}",
                        color = colorResource(id = R.color.aircheck_white),
                        fontSize = TextUnit(20.0F, TextUnitType.Sp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "${forecast.avg}",
                        color = colorResource(id = R.color.aircheck_white),
                        fontSize = TextUnit(50.0F, TextUnitType.Sp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "${forecast.max}",
                        color = colorResource(id = R.color.aircheck_white),
                        fontSize = TextUnit(20.0F, TextUnitType.Sp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(text = descriptionData.quality,
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(16.0F, TextUnitType.Sp),
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
                yesterdayDescriptionData = yesterdayForecastData?.let { Utils.getDescriptionDataForAqiScore(it.avg) },
                tomorrowForecastData = tomorrowForecastData,
                tomorrowDescriptionData = tomorrowForecastData?.let { Utils.getDescriptionDataForAqiScore(it.avg) }
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