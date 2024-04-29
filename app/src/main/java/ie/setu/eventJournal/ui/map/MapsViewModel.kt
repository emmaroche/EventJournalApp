package ie.setu.eventJournal.ui.map

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import timber.log.Timber


@SuppressLint("MissingPermission")
class MapsViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var map : GoogleMap
    var currentLocation = MutableLiveData<Location>()
    var locationClient : FusedLocationProviderClient
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(5000)
        .setMaxUpdateDelayMillis(15000)
        .build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            currentLocation.value = locationResult.locations.last()
        }
    }
    init {
        locationClient = LocationServices.getFusedLocationProviderClient(application)
        locationClient.requestLocationUpdates(locationRequest, locationCallback,
            Looper.getMainLooper())
    }
    fun updateCurrentLocation() {
        if(locationClient.lastLocation.isSuccessful)
            locationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    currentLocation.value = location!!
                    Timber.i("MAP VM LOC SUCCESS: %s", currentLocation.value)
                }
        else // Couldn't get Last Location
            currentLocation.value = Location("Default").apply {
                latitude = 52.245696
                longitude = -7.139102
            }
        Timber.i("MAP VM LOC : %s", currentLocation.value)
    }
}
