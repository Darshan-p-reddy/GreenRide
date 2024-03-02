package com.example.greenride

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONException
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import java.util.regex.Pattern




class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var pickupLatLng: LatLng? = null
    private var dropLatLng: LatLng? = null

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private lateinit var pickupLocationEditText: EditText
    private lateinit var dropLocationEditText: EditText

    private var pickupMarker: Marker? = null
    private var dropMarker: Marker? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val REQUEST_PICKUP_LOCATION = 1
        private const val REQUEST_DROP_LOCATION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Initialize Places SDK
        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        pickupLocationEditText = findViewById(R.id.currentLocation)
        dropLocationEditText = findViewById(R.id.droploc_text)

        pickupLocationEditText.setOnClickListener { showPlaceAutocomplete(REQUEST_PICKUP_LOCATION) }
        dropLocationEditText.setOnClickListener { showPlaceAutocomplete(REQUEST_DROP_LOCATION) }

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        val currentlocButton: ImageButton = findViewById(R.id.currentlocButton)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val btnOpenDrawer: ImageButton = findViewById(R.id.btnOpenDrawer)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        val conformButton: Button = findViewById(R.id.conformbutton)
        val padding = resources.getDimensionPixelSize(R.dimen.map_padding)

        // Add click listener to currentButton
        currentlocButton.setOnClickListener {
            // Check for location permission
            if (isLocationPermissionGranted()) {
                // Permission granted, get current location
                getCurrentLocationForMarker()
            } else {
                // Request location permission
                requestLocationPermission()
            }
        }

        conformButton.setOnClickListener {
            val pickupLocation = pickupLocationEditText.text.toString().trim()
            val dropLocation = dropLocationEditText.text.toString().trim()

            // Check if both pickup and drop locations are selected
            if (pickupLocation.isNotEmpty() && dropLocation.isNotEmpty()) {
                // Both pickup and drop locations are selected, proceed to open the layout
                val intent = Intent(this@MainActivity, drivers::class.java)
                startActivity(intent)
            } else {
                // If either pickup or drop location is not selected, show a toast message
                Toast.makeText(this@MainActivity, "Please select both pickup and drop locations", Toast.LENGTH_SHORT).show()
            }
        }

        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.naveMenu -> {
                    startActivity(Intent(this@MainActivity, PaymentActivity::class.java))

                    Toast.makeText(this@MainActivity, "Payments", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.naveShareRides -> {
                    startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                    Toast.makeText(this@MainActivity, "Your Profile", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.naveRides -> {
                    startActivity(Intent(this@MainActivity, my_rides::class.java))

                    Toast.makeText(this@MainActivity, "My Rides", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.naveAbout -> {
                    startActivity(Intent(this@MainActivity, about_us::class.java))

                    Toast.makeText(this@MainActivity, "About us", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.naveHelp -> {
                    startActivity(Intent(this@MainActivity, contact_us::class.java))

                    Toast.makeText(this@MainActivity, "Contact us", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.naveLogout -> {
                    // Logout
                    FirebaseAuth.getInstance().signOut()
                    // Redirect to Login activity or perform any other action after logout
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish() // Close current activity after logout
                    Toast.makeText(this@MainActivity, "Logged out", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    private fun getCurrentLocationForMarker() {
        // Check if location permission is granted
        if (isLocationPermissionGranted()) {
            // Use FusedLocationProviderClient to get the current location
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            moveMarkerToLocation(currentLatLng) // Move marker to current location
                        } else {
                            // Handle null location
                            Toast.makeText(this, "Unable to retrieve current location", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Toast.makeText(this, "Error getting current location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: SecurityException) {
                // Handle SecurityException
                e.printStackTrace() // Log the exception
                Toast.makeText(this, "Permission denied. Unable to retrieve current location.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Request location permission if not granted
            requestLocationPermission()
        }
    }

    private fun moveMarkerToLocation(location: LatLng) {
        // Remove any existing marker
        pickupMarker?.remove()
        // Add a new marker at the current location
        pickupMarker = googleMap.addMarker(MarkerOptions()
            .position(location)
            .title("Current Location")
            .icon(BitmapDescriptorFactory.fromBitmap(vectorToBitmap(R.drawable.blue_current_location))) // Customize the marker icon if needed
            .draggable(true)) // Make the marker draggable
        // Update the pickupLatLng with the new location
        pickupLatLng = location
        // Move camera to the new location
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }



    override fun onStart() {
        super.onStart()
        mapView.onStart()
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

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun showPlaceAutocomplete(requestCode: Int) {
        val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setTypeFilter(TypeFilter.ADDRESS)
            .build(this)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            when (requestCode) {
                REQUEST_PICKUP_LOCATION -> {
                    pickupLocationEditText.setText(place.name)
                    updateMarker(pickupMarker, place.name, place.latLng)
                }
                REQUEST_DROP_LOCATION -> {
                    dropLocationEditText.setText(place.name)
                    updateMarker(dropMarker, place.name, place.latLng)
                    drawPolyline()
                }
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data!!)
            // Handle error
            Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        // Check if location permission is granted
        if (isLocationPermissionGranted()) {
            // Permission granted, get current location
            getCurrentLocation()
        } else {
            // Request location permission
            requestLocationPermission()
        }

        }
    private fun vectorToBitmap(@DrawableRes vectorResourceId: Int): Bitmap {
        // Load the vector drawable
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResourceId) ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        // Create a Bitmap with appropriate dimensions
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        // Create a Canvas to draw the vector drawable onto the Bitmap
        val canvas = Canvas(bitmap)
        // Set bounds for the drawable
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        // Draw the vector onto the Canvas
        vectorDrawable.draw(canvas)
        // Return the Bitmap
        return bitmap
    }


    private fun updateMarker(marker: Marker?, title: String, position: LatLng? = null) {
        if (marker != null) {
            // Update the existing marker's title
            marker.title = title
            // Update the marker's position if a new position is provided
            position?.let { marker.position = it }
            // Update pickupLatLng or dropLatLng based on the marker type
            if (marker == pickupMarker) {
                pickupLatLng = position
                // Set custom marker icon for pickup marker
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(vectorToBitmap(R.drawable.mappin_svg)))
            } else if (marker == dropMarker) {
                dropLatLng = position
                // Set custom marker icon for drop marker
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(vectorToBitmap(R.drawable.drop_pin_svg)))
            }
            // Debug log messages to check the values of pickupLatLng and dropLatLng
            Log.d("updateMarker", "pickupLatLng: $pickupLatLng")
            Log.d("updateMarker", "dropLatLng: $dropLatLng")
            // Check if both pickup and drop locations are selected
            if (pickupLatLng != null && dropLatLng != null) {
                // Both pickup and drop locations are selected, adjust camera to display both locations
                adjustCameraToShowBothLocations()
                // Draw polyline between pickup and drop locations
                drawPolyline()
            }
        } else {
            // If no marker exists, add a new marker at the provided position
            position?.let {
                googleMap.addMarker(MarkerOptions().position(it).title(title)
                    .icon(BitmapDescriptorFactory.fromBitmap(vectorToBitmap(R.drawable.drop_pin_svg))))
            }
        }
    }


    private fun adjustCameraToShowBothLocations() {
        // Create a bounds builder to include both pickup and drop locations
        val boundsBuilder = LatLngBounds.builder()
        pickupLatLng?.let { boundsBuilder.include(it) }
        dropLatLng?.let { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()
        // Calculate padding to leave some space around the bounding box
        val padding = resources.getDimensionPixelSize(R.dimen.map_padding)
        // Move camera to show both locations with padding
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }


    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun getCurrentLocation() {
        // Check if location permission is granted
        if (isLocationPermissionGranted()) {
            // Use FusedLocationProviderClient to get the current location
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            // Zoom to current location
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                            // Add marker to current location
                            googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location")  .icon(BitmapDescriptorFactory.fromBitmap(vectorToBitmap(
                                R.drawable.blue_current_location
                            ))))
                        } else {
                            // Handle null location
                            Toast.makeText(this, "Unable to retrieve current location", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Toast.makeText(this, "Error getting current location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: SecurityException) {
                // Handle SecurityException
                e.printStackTrace() // Log the exception
                Toast.makeText(this, "Permission denied. Unable to retrieve current location.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Request location permission if not granted
            requestLocationPermission()
        }
    }


    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this@MainActivity)
        val addresses: List<Address>? =
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        return addresses?.firstOrNull()?.getAddressLine(0) ?: ""
    }

    // Modify the updateMarker function to update pickupLatLng and dropLatLng


    private fun drawPolyline() {
        if (pickupLatLng != null && dropLatLng != null) {
            // Construct the URL for the Directions API request
            val apiKey = getString(R.string.google_maps_key)
            val origin = "${pickupLatLng!!.latitude},${pickupLatLng!!.longitude}"
            val destination = "${dropLatLng!!.latitude},${dropLatLng!!.longitude}"
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=$origin&destination=$destination&key=$apiKey"

            // Create a request object
            val request = Request.Builder()
                .url(url)
                .build()

            // Create an HTTP client to send the request
            val client = OkHttpClient()

            // Send the request asynchronously
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle request failure
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Error fetching directions: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("DrawPolyline", "Failed to fetch directions: ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()

                    try {
                        // Parse the JSON response
                        val json = JSONObject(responseBody)
                        val routes = json.getJSONArray("routes")

                        if (routes.length() > 0) {
                            val route = routes.getJSONObject(0)
                            val points = route.getJSONObject("overview_polyline").getString("points")

                            // Decode the polyline points
                            val decodedPoints = PolylineDecoder.decodePolyline(points)

                            // Draw the polyline on the map
                            runOnUiThread {
                                googleMap.addPolyline(PolylineOptions()
                                    .addAll(decodedPoints)
                                    .color(ContextCompat.getColor(this@MainActivity, R.color.black))
                                    .width(12f)
                                )
                                Log.d("DrawPolyline", "Polyline drawn successfully")
                            }
                        } else {
                            // Handle no routes found
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "No routes found between the pickup and drop locations",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.w("DrawPolyline", "No routes found")
                            }
                        }
                    } catch (e: JSONException) {
                        // Handle JSON parsing error
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Error parsing JSON response: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("DrawPolyline", "JSON parsing error: ${e.message}")
                        }
                    }
                }
            })
        } else {
            // Handle null pickupLatLng or dropLatLng
            Log.e("DrawPolyline", "PickupLatLng or dropLatLng is null")
        }
    }
    object PolylineDecoder {
        /**
         * Decode a polyline string into a list of LatLng points.
         * @param polylineString The polyline string to decode.
         * @return A list of decoded LatLng points.
         */
        fun decodePolyline(polylineString: String): List<LatLng> {
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = polylineString.length
            var lat = 0
            var lng = 0
            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = polylineString[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat
                shift = 0
                result = 0
                do {
                    b = polylineString[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng
                val p = LatLng(
                    lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5
                )
                poly.add(p)
            }
            return poly
        }
    }


}
