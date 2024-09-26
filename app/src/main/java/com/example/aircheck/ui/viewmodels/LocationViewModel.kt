package com.example.aircheck.ui.viewmodels

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aircheck.data.Location
import com.example.aircheck.data.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    val locationsUiState: StateFlow<LocationsUiState> =
        locationRepository.getAllLocationsStream().map { LocationsUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = LocationsUiState()
            )

    private val _searchSuggestions = MutableLiveData<List<Address>>(listOf())
    val searchSuggestions: LiveData<List<Address>> = _searchSuggestions

    fun saveLocationToHistory(location: Location) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                locationRepository.insertLocation(location)
            }
        }
    }

    fun getSearchSuggestions(query: String, geocoder: Geocoder) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(
                    query,
                    5
                ) { suggestions ->
                    Log.e("[SearchSuggestions]", "received results: $suggestions")
                    _searchSuggestions.postValue(suggestions)
                }
            } else {
                _searchSuggestions.postValue(geocoder.getFromLocationName(
                    query,
                    5
                ))
            }
        } catch(e: Exception) {
            Log.e("[SearchSuggestions]", e.message ?: "")
        }
    }

    fun clearLocationHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                locationRepository.deleteHistory()
            }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class LocationsUiState(val locationHistory: List<Location> = listOf())