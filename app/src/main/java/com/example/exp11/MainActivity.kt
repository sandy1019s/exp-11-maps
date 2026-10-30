package com.example.exp11

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    data class CityData(
        val name: String,
        val state: String,
        val latLng: LatLng,
        val description: String,
        val chipId: Int,
        val hue: Float
    )

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // UI Components
    private lateinit var headerCard: MaterialCardView
    private lateinit var actvCitySearch: AutoCompleteTextView
    private lateinit var btnClearSearch: ImageButton
    private lateinit var chipGroupCities: ChipGroup
    private lateinit var toggleGroupMapType: MaterialButtonToggleGroup
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var cardCityDetail: MaterialCardView
    private lateinit var tvCityTitle: TextView
    private lateinit var tvCityState: TextView
    private lateinit var tvCityCoordinates: TextView
    private lateinit var tvCityDescription: TextView
    private lateinit var btnZoomIn: MaterialButton
    private lateinit var btnExploreCity: MaterialButton
    private lateinit var btnCloseCard: ImageButton

    private val cityList = mutableListOf<CityData>()
    private val cityMarkerMap = mutableMapOf<String, Marker>()
    private var selectedCity: CityData? = null

    // Location Permission Launcher
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        if (fineLocationGranted || coarseLocationGranted) {
            enableMyLocationOnMap()
            fetchDeviceLocation()
        } else {
            Toast.makeText(
                this,
                getString(R.string.location_permission_needed),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupEdgeToEdgeInsets()
        initCityData()
        setupSearchAutoComplete()
        setupChipGroupListeners()
        setupMapTypeToggle()

        // Initialize Map Fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun initViews() {
        headerCard = findViewById(R.id.headerCard)
        actvCitySearch = findViewById(R.id.actvCitySearch)
        btnClearSearch = findViewById(R.id.btnClearSearch)
        chipGroupCities = findViewById(R.id.chipGroupCities)
        toggleGroupMapType = findViewById(R.id.toggleGroupMapType)
        fabMyLocation = findViewById(R.id.fabMyLocation)
        cardCityDetail = findViewById(R.id.cardCityDetail)
        tvCityTitle = findViewById(R.id.tvCityTitle)
        tvCityState = findViewById(R.id.tvCityState)
        tvCityCoordinates = findViewById(R.id.tvCityCoordinates)
        tvCityDescription = findViewById(R.id.tvCityDescription)
        btnZoomIn = findViewById(R.id.btnZoomIn)
        btnExploreCity = findViewById(R.id.btnExploreCity)
        btnCloseCard = findViewById(R.id.btnCloseCard)

        fabMyLocation.setOnClickListener {
            handleMyLocationClick()
        }

        btnCloseCard.setOnClickListener {
            cardCityDetail.visibility = View.GONE
        }

        btnZoomIn.setOnClickListener {
            selectedCity?.let { city ->
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(city.latLng, 15f))
            }
        }

        btnExploreCity.setOnClickListener {
            selectedCity?.let { city ->
                Toast.makeText(
                    this,
                    "Exploring ${city.name} - ${city.state}",
                    Toast.LENGTH_SHORT
                ).show()
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(city.latLng, 14f))
            }
        }

        btnClearSearch.setOnClickListener {
            actvCitySearch.setText("")
            btnClearSearch.visibility = View.GONE
            hideKeyboard()
        }
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerCard.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun initCityData() {
        cityList.clear()
        cityList.add(
            CityData(
                name = getString(R.string.city_chennai),
                state = getString(R.string.state_chennai),
                latLng = LatLng(13.0827, 80.2707),
                description = getString(R.string.desc_chennai),
                chipId = R.id.chipChennai,
                hue = BitmapDescriptorFactory.HUE_RED
            )
        )
        cityList.add(
            CityData(
                name = getString(R.string.city_bengaluru),
                state = getString(R.string.state_bengaluru),
                latLng = LatLng(12.9716, 77.5946),
                description = getString(R.string.desc_bengaluru),
                chipId = R.id.chipBengaluru,
                hue = BitmapDescriptorFactory.HUE_AZURE
            )
        )
        cityList.add(
            CityData(
                name = getString(R.string.city_mumbai),
                state = getString(R.string.state_mumbai),
                latLng = LatLng(19.0760, 72.8777),
                description = getString(R.string.desc_mumbai),
                chipId = R.id.chipMumbai,
                hue = BitmapDescriptorFactory.HUE_ORANGE
            )
        )
        cityList.add(
            CityData(
                name = getString(R.string.city_delhi),
                state = getString(R.string.state_delhi),
                latLng = LatLng(28.6139, 77.2090),
                description = getString(R.string.desc_delhi),
                chipId = R.id.chipDelhi,
                hue = BitmapDescriptorFactory.HUE_VIOLET
            )
        )
        cityList.add(
            CityData(
                name = getString(R.string.city_hyderabad),
                state = getString(R.string.state_hyderabad),
                latLng = LatLng(17.3850, 78.4867),
                description = getString(R.string.desc_hyderabad),
                chipId = R.id.chipHyderabad,
                hue = BitmapDescriptorFactory.HUE_CYAN
            )
        )
    }

    private fun setupSearchAutoComplete() {
        val cityNames = cityList.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityNames)
        actvCitySearch.setAdapter(adapter)

        actvCitySearch.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedName = parent.getItemAtPosition(position) as String
                val city = cityList.find { it.name.equals(selectedName, ignoreCase = true) }
                city?.let {
                    selectCity(it, moveCamera = true, zoomLevel = 12f)
                }
                btnClearSearch.visibility = View.VISIBLE
                hideKeyboard()
            }
    }

    private fun setupChipGroupListeners() {
        chipGroupCities.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChipId = checkedIds[0]
                val city = cityList.find { it.chipId == checkedChipId }
                city?.let {
                    selectCity(it, moveCamera = true, zoomLevel = 12f)
                }
            }
        }
    }

    private fun setupMapTypeToggle() {
        toggleGroupMapType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && ::mMap.isInitialized) {
                when (checkedId) {
                    R.id.btnTypeNormal -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    R.id.btnTypeSatellite -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    R.id.btnTypeTerrain -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    R.id.btnTypeHybrid -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false

        addCityMarkers()

        // Initial Camera focus on Chennai
        val defaultCity = cityList.firstOrNull { it.name == getString(R.string.city_chennai) }
            ?: cityList.first()
        selectCity(defaultCity, moveCamera = true, zoomLevel = 11f)

        mMap.setOnMarkerClickListener { marker ->
            val city = cityList.find { it.name.equals(marker.title, ignoreCase = true) }
            city?.let {
                selectCity(it, moveCamera = true, zoomLevel = 12f)
            }
            marker.showInfoWindow()
            true
        }

        mMap.setOnMapClickListener {
            // Dismiss focus on tap
            actvCitySearch.clearFocus()
            hideKeyboard()
        }

        // Enable location layer if permission is already granted
        if (hasLocationPermission()) {
            enableMyLocationOnMap()
        }
    }

    private fun addCityMarkers() {
        cityMarkerMap.clear()
        for (city in cityList) {
            val markerOptions = MarkerOptions()
                .position(city.latLng)
                .title(city.name)
                .snippet(city.state)
                .icon(BitmapDescriptorFactory.defaultMarker(city.hue))

            val marker = mMap.addMarker(markerOptions)
            marker?.let {
                cityMarkerMap[city.name] = it
            }
        }
    }

    private fun selectCity(city: CityData, moveCamera: Boolean, zoomLevel: Float) {
        selectedCity = city

        // Update Bottom Detail Card
        tvCityTitle.text = city.name
        tvCityState.text = city.state
        tvCityCoordinates.text = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", city.latLng.latitude, city.latLng.longitude)
        tvCityDescription.text = city.description
        cardCityDetail.visibility = View.VISIBLE

        // Update Selected Chip in ChipGroup
        val targetChip = findViewById<Chip>(city.chipId)
        if (targetChip != null && !targetChip.isChecked) {
            targetChip.isChecked = true
        }

        // Show marker info window
        val marker = cityMarkerMap[city.name]
        marker?.showInfoWindow()

        // Smoothly animate camera
        if (moveCamera) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(city.latLng, zoomLevel))
        }
    }

    private fun handleMyLocationClick() {
        if (hasLocationPermission()) {
            enableMyLocationOnMap()
            fetchDeviceLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocationOnMap() {
        try {
            if (::mMap.isInitialized && hasLocationPermission()) {
                mMap.isMyLocationEnabled = true
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchDeviceLocation() {
        try {
            if (!hasLocationPermission()) return

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
                    Toast.makeText(
                        this,
                        "Current Location: ${location.latitude.toString().take(7)}, ${location.longitude.toString().take(7)}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Unable to fetch current location. Check if GPS is enabled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}
