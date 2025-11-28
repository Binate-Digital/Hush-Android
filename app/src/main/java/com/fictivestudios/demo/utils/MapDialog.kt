package com.fictivestudios.demo.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.fictivestudios.demo.R
import com.fictivestudios.demo.data.responses.MapResponse
import com.fictivestudios.demo.databinding.MapDialogFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MapDialog : DialogFragment(), GoogleMap.OnCameraIdleListener,
    GooglePlaceHelper.GooglePlaceDataInterface,
    View.OnClickListener {
    private lateinit var binding: MapDialogFragmentBinding

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var marker: Marker
    private var googlePlaceHelper: GooglePlaceHelper? = null
    var listener: MapDialogListener? = null

    var latitude: Double? = null
    var longitude: Double? = null
    var locationName: String? = null
    var city: String? = null
    var state: String? = null

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        // Check and request location permissions
        this.googleMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showCurrentLocation(googleMap)

            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.setOnCameraIdleListener(this)
        } else {
            // Request permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, show current location
                mapView.getMapAsync(callback)
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MapDialogFragmentStyle)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MapDialogFragmentBinding.inflate(inflater, container, false)
        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        binding.selectLocationButton.isEnabled = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setOnClickListener()
    }

    private fun initialize() {
        mapView.getMapAsync(callback)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun setOnClickListener() {
        binding.searchTextView.setOnClickListener(this)
        binding.selectLocationButton.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.searchTextView.id -> {
                googlePlaceHelper =
                    GooglePlaceHelper(
                        requireActivity(),
                        GooglePlaceHelper.PLACE_PICKER,
                        this,
                        this,
                        true
                    )
                googlePlaceHelper?.openAutocompleteActivity()
            }

            binding.selectLocationButton.id -> {
                if (locationName == "") {
                    Toast.makeText(
                        binding.selectLocationButton.context,
                        "location Name can't be null",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                sendDataBack(
                    MapResponse(
                        locationName ?: "",
                        state ?: "",
                        city ?: "",
                        longitude ?: 0.0,
                        latitude ?: 0.0, LatLng(latitude ?: 0.0, longitude ?: 0.0)
                    )
                )
            }

            binding.ivBack.id -> {
                dismiss()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode != RESULT_CANCELED){
            if (requestCode == GooglePlaceHelper.REQUEST_CODE_PLACE_HELPER) {
                super.onActivityResult(requestCode, resultCode, data)
                googlePlaceHelper?.onActivityResultAutoCompleted(requestCode, resultCode, data)
            }
        }

    }

    override fun onPlaceActivityResult(longitude: Double, latitude: Double, locationName: String?) {
        Log.d("CheckMap", "onPlaceActivityResult: ${latitude}")
        this.locationName = locationName
        binding.searchTextView.text = locationName
        this.latitude = latitude // Assign latitude directly
        this.longitude = longitude // Assign longitude directly
        val selectedLocation = LatLng(latitude, longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(selectedLocation))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f))
        updateMarkerAndLocationName(LatLng(latitude, longitude))
        binding.selectLocationButton.isEnabled = true

        // Move the camera to the selected location

    }

    override fun onError(error: String?) {
showToast(requireContext(),error.toString())
    }


    override fun onCameraIdle() {
        val target = googleMap.cameraPosition.target
        updateMarkerAndLocationName(target)
    }


    @SuppressLint("MissingPermission")
    private fun showCurrentLocation(googleMap: GoogleMap) {
        if (latitude != null || longitude != null || locationName != null) {
            val currentLatLng = LatLng(latitude ?: 0.0, longitude ?: 0.0)
            marker = googleMap.addMarker(
                MarkerOptions().position(currentLatLng)
            )!!
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            binding.searchTextView.text = locationName
        } else {
            // Get last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.addMarker(
                        MarkerOptions().position(currentLatLng)
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
        binding.selectLocationButton.isEnabled = true
    }


    private fun updateMarkerAndLocationName(latLng: LatLng) {
        // Remove the previous marker if it exists
        if (::marker.isInitialized) {
            marker.remove()
        }

        // Add new marker
        marker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
        )!!

        // Fetch location name using reverse geocoding
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

        if (addresses?.isNotEmpty() == true) {
            val address = addresses[0]
            locationName = address.getAddressLine(0)
            binding.searchTextView.text = address.getAddressLine(0)
            latitude = address.latitude
            longitude = address.longitude
            city = address.locality
            state = address.adminArea
            marker.snippet = locationName
        }
        binding.selectLocationButton.isEnabled = true
    }


    private fun sendDataBack(data: MapResponse) {
        listener?.onDataPassed(data)
        dismiss()
    }

    interface MapDialogListener {
        fun onDataPassed(data: MapResponse)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}