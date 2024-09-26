package com.example.aircheck.ui

import com.example.aircheck.R
import com.example.aircheck.data.AqiSuccessResponse
import com.example.aircheck.data.Attribution
import com.example.aircheck.data.City
import com.example.aircheck.data.DailyForecast
import com.example.aircheck.data.Data
import com.example.aircheck.data.Debug
import com.example.aircheck.data.Forecast
import com.example.aircheck.data.ForecastData
import com.example.aircheck.data.IAQI
import com.example.aircheck.data.Measurement
import com.example.aircheck.data.Time

object Utils {

    fun getDescriptionDataForAqiScore(aqi: Int): AqiDescriptionData {
        return when(aqi) {
            in 0..50 -> {
                AqiDescriptionData(
                    "Good",
                    null,
                    R.color.aqi_good
                )
            }
            in 51..100 -> {
                AqiDescriptionData(
                    "Moderate",
                    "Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion.",
                    R.color.aqi_moderate
                )
            }
            in 101..150 -> {
                AqiDescriptionData(
                    "Unhealthy for Sensitive Groups",
                    "Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion.",
                    R.color.aqi_unhealthy_for_sg
                )
            }
            in 151..200 -> {
                AqiDescriptionData(
                    "Unhealthy",
                    "Active children and adults, and people with respiratory disease, such as asthma, should avoid prolonged outdoor exertion; everyone else, especially children, should limit prolonged outdoor exertion.",
                    R.color.aqi_unhealthy
                )
            }
            in 201..300 -> {
                AqiDescriptionData(
                    "Very Unhealthy",
                    "Active children and adults, and people with respiratory disease, such as asthma, should avoid all outdoor exertion; everyone else, especially children, should limit outdoor exertion.",
                    R.color.aqi_very_unhealthy
                )
            }
            else -> {
                AqiDescriptionData(
                    "Hazardous",
                    "Everyone should avoid all outdoor exertion.",
                    R.color.aqi_hazardous
                )
            }
        }
    }

    fun getExampleResponse(): AqiSuccessResponse {
        return AqiSuccessResponse(
            status = "ok",
            data = Data(
                aqi = 577,
                idx = 12456,
                attributions = listOf(
                    Attribution(
                        url = "http://cpcb.nic.in/",
                        name = "CPCB - India Central Pollution Control Board",
                        logo = "India-CPCB.png"
                    ),
                    Attribution(
                        url = "https://waqi.info/",
                        name = "World Air Quality Index Project",
                        logo = ""
                    )
                ),
                city = City(
                    geo = listOf(19.10078, 72.87462),
                    name = "Chhatrapati Shivaji Intl. Airport (T2), Mumbai, India",
                    url = "https://aqicn.org/city/india/mumbai/chhatrapati-shivaji-intl.-airport-t2",
                    location = ""
                ),
                dominentpol = "pm25",
                iaqi = IAQI(
                    h = Measurement(v = 99.9),
                    no2 = Measurement(v = 5.4),
                    o3 = Measurement(v = 3.0),
                    p = Measurement(v = 1002.86),
                    pm25 = Measurement(v = 577.0),
                    t = Measurement(v = 26.5),
                    w = Measurement(v = 0.84),
                    wg = Measurement(v = 12.8)
                ),
                time = Time(
                    s = "2024-09-25 19:00:00",
                    tz = "+05:30",
                    v = 1727290800,
                    iso = "2024-09-25T19:00:00+05:30"
                ),
                forecast = Forecast(
                    daily = DailyForecast(
                        o3 = listOf(
                            ForecastData(avg = 5, day = "2024-09-23", max = 10, min = 1),
                            ForecastData(avg = 4, day = "2024-09-24", max = 9, min = 1),
                            ForecastData(avg = 1, day = "2024-09-25", max = 5, min = 1),
                            ForecastData(avg = 3, day = "2024-09-26", max = 6, min = 1),
                            ForecastData(avg = 4, day = "2024-09-27", max = 9, min = 1),
                            ForecastData(avg = 4, day = "2024-09-28", max = 11, min = 1),
                            ForecastData(avg = 1, day = "2024-09-29", max = 8, min = 1),
                            ForecastData(avg = 1, day = "2024-09-30", max = 1, min = 1)
                        ),
                        pm10 = listOf(
                            ForecastData(avg = 50, day = "2024-09-23", max = 58, min = 46),
                            ForecastData(avg = 46, day = "2024-09-24", max = 46, min = 46),
                            ForecastData(avg = 46, day = "2024-09-25", max = 46, min = 42),
                            ForecastData(avg = 31, day = "2024-09-26", max = 46, min = 19),
                            ForecastData(avg = 20, day = "2024-09-27", max = 46, min = 12),
                            ForecastData(avg = 30, day = "2024-09-28", max = 42, min = 21),
                            ForecastData(avg = 27, day = "2024-09-29", max = 36, min = 19),
                            ForecastData(avg = 56, day = "2024-09-30", max = 85, min = 28),
                            ForecastData(avg = 43, day = "2024-10-01", max = 46, min = 28)
                        ),
                        pm25 = listOf(
                            ForecastData(avg = 127, day = "2024-09-23", max = 138, min = 89),
                            ForecastData(avg = 116, day = "2024-09-24", max = 138, min = 89),
                            ForecastData(avg = 90, day = "2024-09-25", max = 96, min = 89),
                            ForecastData(avg = 81, day = "2024-09-26", max = 89, min = 58),
                            ForecastData(avg = 64, day = "2024-09-27", max = 89, min = 42),
                            ForecastData(avg = 88, day = "2024-09-28", max = 125, min = 68),
                            ForecastData(avg = 82, day = "2024-09-29", max = 100, min = 68),
                            ForecastData(avg = 143, day = "2024-09-30", max = 171, min = 89),
                            ForecastData(avg = 130, day = "2024-10-01", max = 138, min = 89)
                        ),
                        uvi = listOf(
                            ForecastData(avg = 1, day = "2024-09-23", max = 3, min = 0),
                            ForecastData(avg = 1, day = "2024-09-24", max = 3, min = 0),
                            ForecastData(avg = 0, day = "2024-09-25", max = 2, min = 0),
                            ForecastData(avg = 0, day = "2024-09-26", max = 1, min = 0),
                            ForecastData(avg = 0, day = "2024-09-27", max = 3, min = 0),
                            ForecastData(avg = 1, day = "2024-09-28", max = 6, min = 0),
                            ForecastData(avg = 2, day = "2024-09-29", max = 9, min = 0),
                            ForecastData(avg = 0, day = "2024-09-30", max = 0, min = 0)
                        )
                    )
                ),
                debug = Debug(sync = "2024-09-25T23:05:04+09:00")
            )
        )
    }
}

data class AqiDescriptionData(
    val quality: String,
    val cautionaryStatement: String?,
    val colorId: Int
)