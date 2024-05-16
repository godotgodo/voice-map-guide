package com.example.yazlab

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.internal.PolylineEncoding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class MainActivity : FragmentActivity() , TextToSpeech.OnInitListener  {

    lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    var speechDestination : String? = ""
    var oldDestination : String = ""

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            9 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    print("bildin geç")
                } else {
                    print("Arıza var.")
                }
                return
            }
            // Add more cases if you have multiple permissions to request
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                9
            )
        }




        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        tts = TextToSpeech(this, this)

        if(SpeechRecognizer.isRecognitionAvailable(this))
        {
            print(true)
        }

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act)

        // Get the SupportMapFragment instance
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        // Initialize the GoogleMap object
        mapFragment.getMapAsync { googleMap ->
            // Set the map type
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

            // Check for location permission
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Get the current location
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                // Zoom to the current location
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            } else {
                // Request location permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
            // Check for location permission
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Enable the My Location layer
                googleMap.isMyLocationEnabled = true

                // Get the current location
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                // Add a marker for the current location
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)

                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }

                // Update the marker when the location changes
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                       /* val latLng = LatLng(location.latitude, location.longitude)

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)) */

                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            } else {
                // Request location permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }



        val myButton: Button = findViewById(R.id.myButton)
        myButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")

            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) {
                    val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    speechDestination = data?.get(0)
                    // Use the speech input as a string
                    Toast.makeText(this@MainActivity, "Speech input: $speechDestination", Toast.LENGTH_SHORT).show()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    println("onpartialresults")
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    println("onevents")
                }

                override fun onError(error: Int) {
                    println(error)
                    Toast.makeText(this@MainActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                }

                override fun onReadyForSpeech(params: Bundle?) {
                    println("onreadyforspeech")
                }

                override fun onBeginningOfSpeech() {
                    println("onbegg")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    TODO("Not yet implemented")
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    TODO("Not yet implemented")
                }

                override fun onEndOfSpeech() {
                    println("onendof")
                }
            })

            speechRecognizer.startListening(intent)


            lifecycleScope.launch(Dispatchers.IO) {
                val repeatIntervalMillis = 10000L

                val handler = Handler(Looper.getMainLooper())
                val runnable = object : Runnable {
                    override fun run() {
                        val editText = findViewById<EditText>(R.id.edText)

                        val apiKey = "APIKEY"
                        val mode = "walking"

                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

                        if (ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.e("Location", "Permission not granted")
                            return
                        }

                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->
                                location?.let { currentLocation ->
                                    val (userLatitude, userLongitude) = currentLocation.latitude to currentLocation.longitude
                                    val currentLocationString = "$userLatitude,$userLongitude"

                                  //  val destinationText = editText.text.toString().replace(" ", "+")
                                    val destinationText = speechDestination?.replace(" ", "+")
                                    val geocodingUrl =
                                        "https://maps.googleapis.com/maps/api/geocode/json?address=$destinationText&key=$apiKey"
                                    val geocodingClient = OkHttpClient()
                                    val geocodingRequest = Request.Builder().url(geocodingUrl).build()

                                    geocodingClient.newCall(geocodingRequest).execute().use { geocodingResponse ->
                                        if (!geocodingResponse.isSuccessful)
                                            println("Tekrar et")

                                        val geocodingJsonResponse = JSONObject(geocodingResponse.body!!.string())
                                        val results = geocodingJsonResponse.getJSONArray("results")

                                        if (results.length() > 0) {
                                            val firstResult = results.getJSONObject(0)
                                            val geometry = firstResult.getJSONObject("geometry")
                                            val location = geometry.getJSONObject("location")
                                            val destinationLatitude = location.getDouble("lat")
                                            val destinationLongitude = location.getDouble("lng")
                                            val destination = "$destinationLatitude,$destinationLongitude"
                                            if(destination != ","){
                                                val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                                                        "origin=$currentLocationString" +
                                                        "&destination=$destination" +
                                                        "&mode=$mode" +
                                                        "&key=$apiKey"

                                                val client = OkHttpClient()
                                                val request = Request.Builder()
                                                    .url(url)
                                                    .build()
                                                client.newCall(request).execute().use { response ->
                                                    if (!response.isSuccessful) throw IOException("Failed to get directions: $response")
                                                    val jsonResponse = JSONObject(response.body!!.string())
                                                    val routes = jsonResponse.getJSONArray("routes")
                                                    if (routes.length() > 0) {
                                                        val firstRoute = routes.getJSONObject(0)
                                                        val legs = firstRoute.getJSONArray("legs")
                                                        val firstLeg = legs.getJSONObject(0)
                                                        val steps = firstLeg.getJSONArray("steps")
                                                        if (steps.length() > 0) {
                                                            val firstStep = steps.getJSONObject(0)
                                                            val distance =
                                                                firstStep.getJSONObject("distance")
                                                            val meters = distance.get("value")
                                                            try {
                                                                val maneuver =
                                                                    firstStep.get("maneuver")
                                                                val instruction =
                                                                    (meters.toString() + " metre sonra " + maneuver.toString())
                                                                print(instruction)

                                                                            speak(instruction)

                                                            } catch (e: Exception) {
                                                                if (e is org.json.JSONException) {
                                                                    val instruction =
                                                                        (meters.toString() + " metre boyunca devam edin")
                                                                    println(instruction)

                                                                        speak(instruction)

                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (routes.length() > 0) {
                                                        val firstRoute = routes.getJSONObject(0)
                                                        val polyline = firstRoute.getJSONObject("overview_polyline").getString("points")

                                                        if(oldDestination != destination || oldDestination == "")
                                                        {
                                                            runOnUiThread {

                                                                drawRoute(polyline, destinationLatitude, destinationLongitude)

                                                            }
                                                       }
                                                        oldDestination = destination
                                                        speechRecognizer.destroy()
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.e("Geocode", "Geocode results not found")
                                        }
                                    }
                                }

                            }

                            .addOnFailureListener { exception ->
                                Log.e("Location", "Failed to get location: ${exception.message}")
                            }

                        handler.postDelayed(this, repeatIntervalMillis)
                    }
                }
                handler.post(runnable)

            }
        }
    }

    fun drawRoute(polyline: String, destinationLat: Double, destinationLng: Double) {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->

            googleMap.clear()
            val decodedPath = PolylineEncoding.decode(polyline)
            val options = PolylineOptions()
            for (point in decodedPath) {
                options.add(LatLng(point.lat, point.lng))
            }
            options.width(10f)
            options.color(Color.Blue.hashCode())
            googleMap.addMarker(MarkerOptions().position(LatLng(destinationLat, destinationLng)).title("Destination"))
            googleMap.addPolyline(options)

        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setSpeechRate(1.0f)
        }
    }
    fun speak(text: String) {
        if (::tts.isInitialized) {
            tts.language = Locale.forLanguageTag("tr")
            tts.setSpeechRate(1.0f)
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "hello_world_utterance")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
