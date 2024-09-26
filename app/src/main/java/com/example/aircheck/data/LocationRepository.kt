package com.example.aircheck.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocationRepository @Inject constructor(private val locationDao: LocationDao) {

    suspend fun insertLocation(location: Location) = locationDao.insert(location)

    fun deleteHistory() = locationDao.deleteHistory()

    fun getAllLocationsStream(): Flow<List<Location>> = locationDao.getAllLocations()

    fun getLocationStream(id: Int): Flow<Location?> = locationDao.getLocation(id)

}