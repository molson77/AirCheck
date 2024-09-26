package com.example.aircheck

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.aircheck.data.Location
import com.example.aircheck.ui.theme.AirCheckTheme
import com.example.aircheck.ui.viewmodels.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: LocationViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }
            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            } else -> {
            // No location access granted.
        }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        setContent {
            AirCheckTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.aircheck_blue)
                ) {
                    LandingPage(
                        viewModel = viewModel,
                        onQueryChanged = {
                            viewModel.getSearchSuggestions(it, geocoder)
                        },
                        onLocationSelected = {
                            startActivity(AqiActivity.getAqiActivityIntent(this@MainActivity, it.latitude, it.longitude))
                        },
                        onSuggestionSelected = {
                            viewModel.saveLocationToHistory(location = it)
                            startActivity(AqiActivity.getAqiActivityIntent(this@MainActivity, it.latitude, it.longitude))
                        },
                        onCurrentLocationSelected = {
                            getCurrentLocation()
                        },
                        onSearchLocation = {

                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // reuqest for permission
            locationPermissionRequest.launch(arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION))
        } else {
            // some level of location permissions already granted
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    startActivity(AqiActivity.getAqiActivityIntent(this@MainActivity, it.latitude, it.longitude))
                } else {
                    Toast.makeText(this, "Location fetch unsuccessful, try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}

@Composable
fun LandingPage(
    viewModel: LocationViewModel,
    onQueryChanged: (String) -> Unit,
    onLocationSelected: (Location) -> Unit,
    onSuggestionSelected: (Location) -> Unit,
    onCurrentLocationSelected: () -> Unit,
    onSearchLocation: (String) -> Unit,
    modifier: Modifier
) {

    val locationsUiState by viewModel.locationsUiState.collectAsState()
    val suggestionsList = viewModel.searchSuggestions.observeAsState().value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Header()
        SearchAndHistory(
            locationList = locationsUiState.locationHistory,
            suggestionsList = suggestionsList ?: listOf(),
            onQueryChanged = {onQueryChanged(it)},
            onLocationSelected = {onLocationSelected(it)},
            onSuggestionSelected = {onSuggestionSelected(it)},
            onCurrentLocationSelected = {onCurrentLocationSelected.invoke()},
            onSearchLocation = {onSearchLocation.invoke(it)}
        )
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(bottom = 10.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.aircheck),
            contentDescription = "AirCheck logo",
            tint = colorResource(id = R.color.aircheck_white),
            modifier = Modifier.fillMaxWidth(0.30F)
        )
        Text(text = stringResource(id = R.string.app_name),
            color = colorResource(id = R.color.aircheck_white),
            fontSize = TextUnit(38.0F, TextUnitType.Sp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        )
        Text(text = "Air Quality Index (AQI) data on demand!",
            color = colorResource(id = R.color.aircheck_white),
            fontSize = TextUnit(14.0F, TextUnitType.Sp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Default,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 36.dp)
        )
    }
}

@Composable
fun SearchAndHistory(
    locationList: List<Location>,
    suggestionsList: List<Address>,
    onQueryChanged: (String) -> Unit,
    onLocationSelected: (Location) -> Unit,
    onSuggestionSelected: (Location) -> Unit,
    onCurrentLocationSelected: () -> Unit,
    onSearchLocation: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var query by remember{ mutableStateOf("") }
    val scrollState = rememberLazyListState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onQueryChanged.invoke(it)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                //onSearchLocation.invoke(query)
                //query = ""
            }),
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.aircheck_white),
                unfocusedBorderColor = colorResource(id = R.color.aircheck_white),
                disabledBorderColor = colorResource(id = R.color.aircheck_white),
                focusedContainerColor = colorResource(id = R.color.aircheck_blue),
                unfocusedContainerColor = colorResource(id = R.color.aircheck_blue),
                disabledContainerColor = colorResource(id = R.color.aircheck_blue),
                focusedTextColor = colorResource(id = R.color.aircheck_white),
                unfocusedTextColor = colorResource(id = R.color.aircheck_white),
                focusedPlaceholderColor = colorResource(id = R.color.aircheck_white),
                unfocusedPlaceholderColor = colorResource(id = R.color.aircheck_white),
                focusedLeadingIconColor = colorResource(id = R.color.aircheck_white),
                unfocusedLeadingIconColor = colorResource(id = R.color.aircheck_white)
            ),
            textStyle = TextStyle(
                colorResource(id = R.color.aircheck_white),
                fontSize = TextUnit(16.0F, TextUnitType.Sp)
            ),
            placeholder = {
                Text(text = "Search location",
                    color = colorResource(id = R.color.aircheck_white),
                    fontSize = TextUnit(16.0F, TextUnitType.Sp),
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Default,
                    maxLines = 2
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    modifier = Modifier.size(26.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            if (query.isNotBlank()) {
                items(items = suggestionsList.filter {
                    it.hasLatitude() && it.hasLongitude()
                }) { address ->
                    SuggestionItem(
                        address = address,
                        onLocationSelected = {
                            onSuggestionSelected.invoke(it)
                        }
                    )
                }
            } else {
                item {
                    CurrentLocationItem(
                        onCurrentLocationSelected = {
                            onCurrentLocationSelected.invoke()
                        }
                    )
                }
                items(items = locationList) { location ->
                    LocationItem(
                        location = location,
                        onLocationSelected = {
                            onLocationSelected.invoke(it)
                        }
                    )
                }
            }
        }

    }
}

@Composable
fun LocationItem(
    location: Location,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clickable { onLocationSelected.invoke(location) },
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painterResource(id = R.drawable.location_arrow),
            "Location",
            Modifier.size(24.dp),
            tint = colorResource(id = R.color.aircheck_white)
        )
        Text(text = location.name,
            color = colorResource(id = R.color.aircheck_white),
            fontSize = TextUnit(18.0F, TextUnitType.Sp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
fun SuggestionItem(
    address: Address,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clickable {
                onLocationSelected.invoke(
                    Location(
                        name = address.getAddressLine(0),
                        latitude = address.latitude,
                        longitude = address.longitude
                    )
                )
            },
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painterResource(id = R.drawable.location_arrow),
            "Location",
            Modifier.size(24.dp),
            tint = colorResource(id = R.color.aircheck_white)
        )
        Text(text = address.getAddressLine(0),
            color = colorResource(id = R.color.aircheck_white),
            fontSize = TextUnit(18.0F, TextUnitType.Sp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
fun CurrentLocationItem(
    onCurrentLocationSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clickable { onCurrentLocationSelected.invoke() },
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painterResource(id = R.drawable.location_crosshairs),
            "Location",
            Modifier.size(24.dp),
            tint = colorResource(id = R.color.aircheck_white)
        )
        Text(text = "Current Location",
            color = colorResource(id = R.color.aircheck_white),
            fontSize = TextUnit(18.0F, TextUnitType.Sp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LocationsPreview() {
    AirCheckTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.aircheck_blue)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Header()
                SearchAndHistory(
                    locationList = listOf(Location(1, "Philadelphia, PA", 0.0, 0.0)),
                    suggestionsList = listOf(),
                    onQueryChanged = {},
                    onLocationSelected = {},
                    onSuggestionSelected = {},
                    onCurrentLocationSelected = {},
                    onSearchLocation = {}
                )
            }
        }
    }
}