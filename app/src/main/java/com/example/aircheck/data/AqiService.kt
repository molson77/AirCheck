package com.example.aircheck.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// example from website:
// https://api.waqi.info/feed/geo:39.818715;-75.413973/?token=3b3231b5ebc02cf2116c7317eeb6d7b760d14fae

interface AqiService {

    companion object {
        const val BASE_URL = "https://api.waqi.info/"
    }

    @GET("feed/geo:{lat};{lng}/")
    suspend fun getAqiDataFromCoordinates(
        @Path("lat") lat: Double,
        @Path("lng") lng: Double,
        @Query(value = "token") token: String
    ) : AqiResponse
}