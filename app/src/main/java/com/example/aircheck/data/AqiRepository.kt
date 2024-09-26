package com.example.aircheck.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AqiRepository @Inject constructor(
    private val aqiService: AqiService
) {

    companion object {
        // TODO: Store API key somewhere else
        const val TOKEN = "3b3231b5ebc02cf2116c7317eeb6d7b760d14fae"
    }

    suspend fun getAqiDataFromCoordinates(lat: Double, lng: Double): Flow<Result<AqiResponse>> {
        return flow<AqiResponse> {
            aqiService.getAqiDataFromCoordinates(lat, lng, TOKEN)
        }.asResult()
    }
}