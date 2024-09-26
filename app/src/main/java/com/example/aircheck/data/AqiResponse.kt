package com.example.aircheck.data


// Types of responses

interface AqiResponse {
    val status: String
}

data class AqiErrorResponse(
    override val status: String,
    val data: String
) : AqiResponse

data class AqiSuccessResponse(
    override val status: String,
    val data: Data,
) : AqiResponse


// Data fields of response

data class Data(
    val aqi: Int,
    val idx: Int,
    val attributions: List<Attribution>,
    val city: City,
    val dominentpol: String,
    val iaqi: IAQI,
    val time: Time,
    val forecast: Forecast,
    val debug: Debug,
)

data class Attribution(
    val url: String,
    val name: String,
    val logo: String,
)

data class City(
    val geo: List<Double>,
    val name: String,
    val url: String,
    val location: String,
)

data class IAQI(
    val h: Measurement,
    val no2: Measurement,
    val o3: Measurement,
    val p: Measurement,
    val pm25: Measurement,
    val t: Measurement,
    val w: Measurement,
    val wg: Measurement,
)

data class Measurement(
    val v: Double,
)

data class Time(
    val s: String,
    val tz: String,
    val v: Long,
    val iso: String,
)

data class Forecast(
    val daily: DailyForecast,
)

data class DailyForecast(
    val o3: List<ForecastData>,
    val pm10: List<ForecastData>,
    val pm25: List<ForecastData>,
    val uvi: List<ForecastData>,
)

data class ForecastData(
    val avg: Int,
    val day: String,
    val max: Int,
    val min: Int,
)

data class Debug(
    val sync: String,
)