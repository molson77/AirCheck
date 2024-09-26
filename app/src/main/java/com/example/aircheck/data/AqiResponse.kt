package com.example.aircheck.data

import com.google.gson.annotations.SerializedName


// Types of responses

interface AqiResponse {
    val status: String
}

data class AqiErrorResponse(
    @SerializedName("status")
    override val status: String,
    @SerializedName("data")
    val data: String
) : AqiResponse

data class AqiSuccessResponse(
    @SerializedName("status")
    override val status: String,
    @SerializedName("data")
    val data: Data = Data(),
) : AqiResponse


// Data fields of response

data class Data(
    @SerializedName("aqi")
    val aqi: String = "",
    @SerializedName("idx")
    val idx: Int = 0,
    @SerializedName("attributions")
    val attributions: List<Attribution> = listOf(),
    @SerializedName("city")
    val city: City = City(),
    @SerializedName("dominentpol")
    val dominentpol: String = "",
    @SerializedName("iaqi")
    val iaqi: IAQI = IAQI(),
    @SerializedName("time")
    val time: Time = Time(),
    @SerializedName("forecast")
    val forecast: Forecast = Forecast(),
    @SerializedName("debug")
    val debug: Debug = Debug(),
)

data class Attribution(
    @SerializedName("url")
    val url: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("logo")
    val logo: String = "",
)

data class City(
    @SerializedName("geo")
    val geo: List<Double> = listOf(),
    @SerializedName("name")
    val name: String = "",
    @SerializedName("url")
    val url: String = "",
    @SerializedName("location")
    val location: String = "",
)

data class IAQI(
    @SerializedName("h")
    val h: Measurement = Measurement(),
    @SerializedName("no2")
    val no2: Measurement = Measurement(),
    @SerializedName("o3")
    val o3: Measurement = Measurement(),
    @SerializedName("p")
    val p: Measurement = Measurement(),
    @SerializedName("pm25")
    val pm25: Measurement = Measurement(),
    @SerializedName("t")
    val t: Measurement = Measurement(),
    @SerializedName("w")
    val w: Measurement = Measurement(),
    @SerializedName("wg")
    val wg: Measurement = Measurement(),
)

data class Measurement(
    @SerializedName("v")
    val v: Double = 0.0,
)

data class Time(
    @SerializedName("s")
    val s: String = "",
    @SerializedName("tz")
    val tz: String = "",
    @SerializedName("v")
    val v: Long = 0L,
    @SerializedName("iso")
    val iso: String = "",
)

data class Forecast(
    @SerializedName("daily")
    val daily: DailyForecast = DailyForecast(),
)

data class DailyForecast(
    @SerializedName("o3")
    val o3: List<ForecastData> = listOf(),
    @SerializedName("pm10")
    val pm10: List<ForecastData> = listOf(),
    @SerializedName("pm25")
    val pm25: List<ForecastData> = listOf(),
    @SerializedName("uvi")
    val uvi: List<ForecastData> = listOf(),
)

data class ForecastData(
    @SerializedName("avg")
    val avg: Int = 0,
    @SerializedName("day")
    val day: String = "",
    @SerializedName("max")
    val max: Int = 0,
    @SerializedName("min")
    val min: Int = 0,
)

data class Debug(
    @SerializedName("sync")
    val sync: String = "",
)