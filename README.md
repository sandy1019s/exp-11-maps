# Experiment 11 – MapVista: Google Maps Android Application

## Aim
To develop a complete, feature-rich Android application (**MapVista – Explore Maps**) that integrates the official **Google Maps SDK for Android** to display an interactive map, support zooming and panning, set initial camera focus to Chennai, render custom markers for major Indian cities with detailed descriptions, provide city search and quick filter chips, toggle map layers, and handle runtime user location permissions.

---

## Software Requirements
- **Operating System:** Windows 10/11, macOS, or Linux
- **IDE:** Android Studio (Ladybug / Meerkat / 2026.1+ or newer)
- **SDK:** Android SDK (API Level 24 to 37)
- **Language:** Kotlin 2.x
- **Build Tool:** Gradle (Kotlin DSL) with Version Catalogs (`libs.versions.toml`)
- **Google Play Services:** Google Maps SDK for Android (`play-services-maps`), Fused Location Provider (`play-services-location`)

---

## Technologies Used
- **Kotlin:** Modern, concise, null-safe Android development language.
- **Google Maps SDK for Android:** `SupportMapFragment`, `GoogleMap`, `MarkerOptions`, `CameraUpdateFactory`, `BitmapDescriptorFactory`.
- **Google Location Services:** `FusedLocationProviderClient` for location management.
- **Material Design 3 (M3):** `MaterialCardView`, `ChipGroup`, `Chip`, `MaterialButtonToggleGroup`, `FloatingActionButton`, `AutoCompleteTextView`.
- **AndroidX & Jetpack:** Edge-to-edge support, `ViewCompat`, `WindowInsetsCompat`, `ActivityResultContracts.RequestMultiplePermissions`.

---

## Google Maps SDK Integration & API Key Configuration

1. **Gradle Dependencies (`app/build.gradle.kts` & `libs.versions.toml`)**:
   - `com.google.android.gms:play-services-maps:19.0.0`
   - `com.google.android.gms:play-services-location:21.3.0`

2. **Obtaining and Configuring your Google Maps API Key**:
   - You must obtain your own Google Maps API key from the [Google Cloud Console](https://console.cloud.google.com/) by enabling the **Maps SDK for Android**.
   - Create or open `local.properties` in the root directory of your project (note: `local.properties` is strictly git-ignored and **must never be committed to GitHub**).
   - Add your API key to `local.properties` as follows:
     ```properties
     MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
     ```
   - In `app/build.gradle.kts`, `local.properties` is automatically loaded and passed as a manifest placeholder:
     ```kotlin
     val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
     manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
     ```

3. **AndroidManifest Setup (`AndroidManifest.xml`)**:
   - Configured permissions: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`.
   - Added `com.google.android.geo.API_KEY` meta-data referencing `${MAPS_API_KEY}`.

---

## Key Features

1. **Interactive Google Map View**:
   - Full-screen interactive map with gestures (pan, zoom, tilt, rotate).
   - Initial focal point centered on **Chennai, Tamil Nadu** (Lat: 13.0827, Lng: 80.2707).

2. **Predefined City Markers & Rich Information**:
   - Custom colored markers for 5 major Indian cities:
     - **Chennai** (Tamil Nadu)
     - **Bengaluru** (Karnataka)
     - **Mumbai** (Maharashtra)
     - **Delhi** (National Capital Territory)
     - **Hyderabad** (Telangana)
   - Tapping any marker opens an info window and smoothly updates an attractive bottom `MaterialCardView` detailing city history, state tag, precise latitude/longitude, and action buttons ("Zoom In", "Explore City").

3. **City Search & Quick Navigation**:
   - Top floating search bar with auto-complete suggestions.
   - Quick filter chip bar (`ChipGroup`) for instant one-tap camera animation to any city.

4. **Map Type Toggle**:
   - Live layer switcher between **Default (Normal)**, **Satellite**, **Terrain**, and **Hybrid** views.

5. **Runtime Location Permissions & Current Location**:
   - Requests `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` dynamically using modern Activity Result API.
   - Activates My Location layer and animates camera to user's real-time position upon tapping the Floating Action Button.

---

## Implementation Procedure

1. **Configure API Key & Dependencies**:
   - Add `MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY` to `local.properties`.
   - Update `libs.versions.toml` and `app/build.gradle.kts` with Google Play Services dependencies and manifest placeholders.
2. **Setup AndroidManifest.xml**:
   - Request location permissions and insert the Google Maps API key meta-data tag.
3. **Design Modern XML Layout (`activity_main.xml`)**:
   - Construct top app bar, search card with auto-complete text view, horizontal city chip group, map type toggle, `SupportMapFragment`, my-location FAB, and bottom detail card.
4. **Implement Kotlin Map Controller (`MainActivity.kt`)**:
   - Implement `OnMapReadyCallback`.
   - Set up `GoogleMap` settings, markers, click listeners, camera animations, auto-complete adapter, chip listeners, map layer switcher, and location permission handler.
5. **Compile & Execute**:
   - Build using `./gradlew assembleDebug` and verify map rendering, marker interaction, camera transitions, and location permissions.

---

## Output Description
- **Initial View:** App launches in edge-to-edge mode displaying "MapVista" top header and Google Map centered on Chennai, Tamil Nadu with a red marker and open bottom info panel.
- **City Selection:** Selecting Bengaluru, Mumbai, Delhi, or Hyderabad via search or chip animates the camera smoothly with a zoom transition, updates marker info window, and populates the bottom card with coordinates and description.
- **Layer Switching:** Toggling Satellite/Terrain/Hybrid updates map tiles instantly.
- **Location Feature:** Tapping the current location FAB prompts location permission dialog and centers map on device location when granted.

---

## Google Cloud Billing Notice
> [!NOTE]
> Google Maps SDK for Android requires an active Google Cloud Platform project with billing enabled for your API key to render vector map tiles. If billing is not activated on the Google Cloud project associated with your API key, Google Maps will display a grid watermark or dialog stating *"This page can't load Google Maps correctly"*. All application logic, markers, UI overlays, location services, and camera animations function as designed.

---

## Result
The Google Maps Android application **MapVista – Explore Maps** was developed, configured with API key security, compiled, and verified successfully.
