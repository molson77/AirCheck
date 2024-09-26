package com.example.aircheck.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location)

    @Query("DELETE from locations")
    fun deleteHistory()

    @Query("SELECT * from locations WHERE id = :id")
    fun getLocation(id: Int): Flow<Location>

    @Query("SELECT * from locations ORDER BY id ASC")
    fun getAllLocations(): Flow<List<Location>>
}