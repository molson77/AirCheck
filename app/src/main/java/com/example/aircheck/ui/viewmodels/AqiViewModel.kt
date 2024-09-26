package com.example.aircheck.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aircheck.data.AqiErrorResponse
import com.example.aircheck.data.AqiRepository
import com.example.aircheck.data.AqiSuccessResponse
import com.example.aircheck.data.ForecastData
import com.example.aircheck.data.Response
import com.example.aircheck.ui.AqiDescriptionData
import com.example.aircheck.ui.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AqiViewModel @Inject constructor(
    private val aqiRepository: AqiRepository
) : ViewModel() {

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
    }

    private val _uiState = MutableStateFlow(AqiUiState())
    val uiState : StateFlow<AqiUiState> = _uiState.asStateFlow()

    init {
        reset()
    }

    fun getAqiData(lat: Double, lng: Double) {
        viewModelScope.launch {
            aqiRepository.getAqiDataFromCoordinates(lat, lng).collect { response ->
                when(response) {
                    is Response.Error -> {
                        // HTTP unsuccessful / caught exception
                        Log.d("[AQI]", "Error for lat:$lat/lng:$lng")
                        _uiState.update { currentState ->
                            currentState.copy(loading = false, errorMessage = "An error occurred, request was unsuccessful.")
                        }
                    }
                    is Response.Loading -> {
                        Log.d("[AQI]", "Loading data for lat:$lat/lng:$lng...")
                        _uiState.update { currentState ->
                            currentState.copy(loading = true)
                        }
                    }
                    is Response.Success -> {
                        // HTTP successful
                        _uiState.update { currentState ->
                            when(response.data) {
                                is AqiSuccessResponse -> {
                                    Log.d("[AQI]", "Retrieved data for lat:$lat/lng:$lng")
                                    // Successful data retrieval
                                    val today = LocalDate.now()
                                    val yesterday = today.minusDays(1).format(DateTimeFormatter.ofPattern(DATE_PATTERN))
                                    val tomorrow = today.plusDays(1).format(DateTimeFormatter.ofPattern(DATE_PATTERN))
                                    val descriptionData = Utils.getDescriptionDataForAqiScore(response.data.data.aqi)
                                    val yesterdayForecastData = response.data.data.forecast.daily.pm25.find { it.day == yesterday }
                                    val tomorrowForecastData = response.data.data.forecast.daily.pm25.find { it.day == tomorrow }

                                    currentState.copy(
                                        response = response.data,
                                        loading = false,
                                        errorMessage = null,
                                        descriptionData = descriptionData,
                                        yesterdayForecastData = yesterdayForecastData,
                                        yesterdayDescriptionData = yesterdayForecastData?.let { Utils.getDescriptionDataForAqiScore(it.avg.toString()) },
                                        tomorrowForecastData = tomorrowForecastData,
                                        tomorrowDescriptionData = tomorrowForecastData?.let { Utils.getDescriptionDataForAqiScore(it.avg.toString()) }
                                    )
                                }
                                is AqiErrorResponse -> {
                                    // Unsuccessful data retrieval
                                    Log.d("[AQI]", "Error for lat:$lat/lng:$lng - ${response.data.data}")
                                    currentState.copy(loading = false, errorMessage = response.data.data)
                                }
                                else -> {
                                    // Unknown response type
                                    Log.d("[AQI]", "Error for lat:$lat/lng:$lng - Unknown response")
                                    currentState.copy(loading = false, errorMessage = "An error occurred, please try again.")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = AqiUiState()
    }
}

data class AqiUiState (
    val response: AqiSuccessResponse? = null,
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val descriptionData: AqiDescriptionData? = null,
    val yesterdayForecastData: ForecastData? = null,
    val yesterdayDescriptionData: AqiDescriptionData? = null,
    val tomorrowForecastData: ForecastData? = null,
    val tomorrowDescriptionData: AqiDescriptionData? = null,
)