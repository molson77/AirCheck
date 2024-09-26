package com.example.aircheck

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.aircheck.data.AqiService
import com.example.aircheck.data.LocationDao
import com.example.aircheck.data.LocationDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideBaseUrl(): String {
        return AqiService.BASE_URL
    }

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                provideLoggingInterceptor()
            )
            .build()
    }

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("[AQI]", message)
                }
            }
        ).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    fun provideRetrofit(
        baseUrl: String,
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton // Tell Dagger-Hilt to create a singleton accessible everywhere in ApplicationCompenent (i.e. everywhere in the application)
    @Provides
    fun provideLocationDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        LocationDatabase::class.java,
        "locations"
    ).build() // The reason we can construct a database for the repo

    @Singleton
    @Provides
    fun provideLocationDao(database: LocationDatabase) : LocationDao = database.locationDao()

    @Provides
    @Singleton
    fun provideAqiService(retrofit: Retrofit): AqiService =
        retrofit.create(AqiService::class.java)

}