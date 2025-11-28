package com.fictivestudios.demo.utils


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.fictivestudios.demo.ui.activities.MainActivity
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.io.IOException
import java.util.Locale


@Suppress("DEPRECATION")
class GooglePlaceHelper(
    private val activity: Activity,
    private val apiType: Int,
    private val googlePlaceDataInterface: GooglePlaceDataInterface,
    private val fragment: Fragment?,
    isFullScreen: Boolean
) {
    private val isFullScreen = true

    /**
     * Call this method in a fragment when you want to open the map
     * CHOOSE MODE_FULLSCREEN OR OVERLAY for Popup
     */
    fun openAutocompleteActivity() {
        // The autocomplete activity requires Google Play Services to be available. The intent
        // builder checks this and throws an exception if it is not the case.
        val intent: Intent
        //            if (apiType == PLACE_PICKER) {
//                intent = new Plac.IntentBuilder()
//                        .build(activity);
//            } else {
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(activity, GEO_API_KEY)
        }

        // Set the fields to specify which types of place data to return.
        val fields: List<Place.Field> =
            listOf<Place.Field>(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // Start the autocomplete intent.
        val autocompleteActivityMode: AutocompleteActivityMode = if (isFullScreen) {
            AutocompleteActivityMode.FULLSCREEN
        } else {
            AutocompleteActivityMode.OVERLAY
        }
        intent = Autocomplete.IntentBuilder(
            autocompleteActivityMode, fields
        )
            .build(activity)
        if (fragment == null) {
            activity.startActivityForResult(intent, REQUEST_CODE_PLACE_HELPER)
        } else {
            fragment.startActivityForResult(intent, REQUEST_CODE_PLACE_HELPER)
        }
    }

    /**
     * Call this method in a fragment when you want to open the map
     */
    fun openMapsActivity() {
        val i = Intent(activity, MainActivity::class.java)
        if (fragment == null) {
            activity.startActivityForResult(i, REQUEST_CODE_PLACE_HELPER)
        } else {
            fragment.startActivityForResult(i, REQUEST_CODE_PLACE_HELPER)
        }
    }

    /**
     * Override fragment's onActivityResult and pass its parameters here.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    fun onActivityResultAutoCompleted(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PLACE_HELPER && data != null) {
            when (resultCode) {
                AutocompleteActivity.RESULT_ERROR -> {
                    Log.d("CheckMap", "onActivityResultAutoCompleted: resultCode = ${resultCode}, data = ${data}")
                    // TODO: Handle the error.
                    val status: Status = Autocomplete.getStatusFromIntent(data)
                    Log.e("CheckMap", "Error: Status = $status")
                    googlePlaceDataInterface.onError(status.toString())
                }
                Activity.RESULT_CANCELED -> {
                    Log.d("CheckMap", "onActivityResultAutoCompleted:  resultCode = ${resultCode}, data = ${data}")
                    // Indicates that the activity closed before a selection was made. For example if
                    // the user pressed the back button.
                }
                else -> {
                    Log.d("CheckMap", "Else:  resultCode = ${resultCode}, data = ${data}")
                    val place: Place = Autocomplete.getPlaceFromIntent(data) as Place
                    val locationName: String = place.name.toString()
                    val latitude: Double = place.latLng?.latitude ?: 0.0
                    val longitude: Double = place.latLng?.longitude ?: 0.0
                    Log.d(
                        TAG,
                        "onActivityResult MAP: locationName = $locationName"
                    )
                    Log.d(
                        TAG,
                        "onActivityResult MAP: latitude = $latitude"
                    )
                    Log.d(
                        TAG,
                        "onActivityResult MAP: longitude = $longitude"
                    )
                    googlePlaceDataInterface.onPlaceActivityResult(longitude, latitude, locationName)
                }
            }
        }
    }

   data class GoogleAddressModel(
        val address: String,
        private val city: String,
        private val state: String,
        private val country: String,
        private val postalCode: String,
        private val streetName: String,
        var lATITUDE: Double,
        var lONGITUDE: Double
    )

    interface GooglePlaceDataInterface {
        fun onPlaceActivityResult(longitude: Double, latitude: Double, locationName: String?)
        fun onError(error: String?)
    }

    companion object {
        const val REQUEST_CODE_PLACE_HELPER = 6666
        const val PLACE_PICKER = 0
        const val GEO_API_KEY = "AIzaSyBmaS0B0qwokES4a_CiFNVkVJGkimXkNsk"
        private const val TAG = "Google Place"

        /**
         * GET ADDRESS from Geo Coder.
         *
         * @param context
         * @param LATITUDE
         * @param LONGITUDE
         * @return
         */
        fun getAddress(context: Context?, LATITUDE: Double, LONGITUDE: Double): GoogleAddressModel {
            var googleAddressModel = GoogleAddressModel("", "", "", "", "", "", LATITUDE, LONGITUDE)

            //Set AddressFragment
            try {
                val geocoder = Geocoder(context!!, Locale.getDefault())
                val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
                if (addresses != null && addresses.size > 0) {
                    val address =
                        addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    val city = addresses[0].locality
                    val state = addresses[0].adminArea
                    val country = addresses[0].countryName
                    val postalCode = addresses[0].postalCode
                    val streetName = addresses[0].featureName // Only if available else return NULL
                    googleAddressModel = GoogleAddressModel(
                        address,
                        city,
                        state,
                        country,
                        postalCode,
                        streetName,
                        LATITUDE,
                        LONGITUDE
                    )
                    Log.d(
                        TAG,
                        "getAddress:  address -$address"
                    )
                    Log.d(TAG, "getAddress:  city -$city")
                    Log.d(
                        TAG,
                        "getAddress:  country -$country"
                    )
                    Log.d(
                        TAG,
                        "getAddress:  state -$state"
                    )
                    Log.d(
                        TAG,
                        "getAddress:  postalCode -$postalCode"
                    )
                    Log.d(
                        TAG,
                        "getAddress:  knownName$streetName"
                    )
                    val countryCode = addresses[0].countryCode
                    Log.d(
                        TAG,
                        "getAddress:  countryCode$countryCode"
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return googleAddressModel
        }

        /**
         * You can change the key with your geo API Key.
         *
         * @param LAT
         * @param LONG
         * @return
         */
        fun getMapSnapshotURL(LAT: Double, LONG: Double): String {
            return "https://maps.googleapis.com/maps/api/staticmap?center=" + LAT + "," + LONG + "&zoom=16&size=512x512&markers=color:red%7C" + LAT + "," + LONG + "&key=" + GEO_API_KEY
        }

        fun intentOpenMap(activity: Context, latitude: Double, longitude: Double, label: String) {
//        double latitude = 25.161156;
//        double longitude = 55.237092;
//        String label = "Label";
            val uriBegin = "geo:$latitude,$longitude"
            val query = "$latitude,$longitude($label)"
            val encodedQuery = Uri.encode(query)
            val uriString = "$uriBegin?q=$encodedQuery&z=20"
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
            }
        }
    }
}
