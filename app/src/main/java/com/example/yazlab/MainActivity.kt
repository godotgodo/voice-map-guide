package com.example.yazlab

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Locale


class MainActivity : ComponentActivity() {

    lateinit var tts: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act)
        val myButton: Button = findViewById(R.id.myButton)
        myButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val editText = findViewById<EditText>(R.id.edText)
                //val (latitude, longitude) = editText.text.toString().split(",").map { it.trim().toDoubleOrNull() ?: return@map null }
                //edittextten alabiliriz şu şekilde şimdilik böyle bırakıyorum
                val (latitude, longitude) = "40.9880, 29.0378".split(",").map { it.trim().toDoubleOrNull() ?: return@map null }
                val apiKey = "api_key"
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
                    return@launch
                }
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        location?.let {
                            val userLatitude = it.latitude
                            val userLongitude = it.longitude
                            val currentLocation = "$userLatitude,$userLongitude"
                            val destination = "37.9880, -120.00000"
                            //şimdilik statik, edText'ten gelen alınacak yukarıda açıklamıştım
                            //val destination = "$latitude,$longitude"
                            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                                    "origin=$currentLocation" +
                                    "&destination=$destination" +
                                    "&mode=$mode" +
                                    "&key=$apiKey"

                            val client = OkHttpClient()
                            val request = Request.Builder()
                                .url(url)
                                .build()
                            try {
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
                                            val distance = firstStep.getJSONObject("distance")
                                            val meters = distance.get("value")
                                            try {
                                                val maneuver = firstStep.get("maneuver")
                                                val instruction=(meters.toString() + " metre sonra " + maneuver.toString())
                                                tts = TextToSpeech(applicationContext) { status ->
                                                    if (status == TextToSpeech.SUCCESS) {
                                                        tts.language = Locale.US
                                                        tts.setSpeechRate(1.0f)
                                                        tts.speak(instruction, TextToSpeech.QUEUE_ADD, null)
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                if (e is org.json.JSONException) {
                                                    val instruction=(meters.toString() + " metre boyunca devam edin")
                                                    println(instruction)
                                                    tts = TextToSpeech(applicationContext) { status ->
                                                        if (status == TextToSpeech.SUCCESS) {
                                                            tts.language = Locale.US
                                                            tts.setSpeechRate(1.0f)
                                                            tts.speak(instruction, TextToSpeech.QUEUE_ADD, null)
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            println("Adım bulunamadı")
                                        }
                                    }
                                }
                            } catch (e: IOException) {
                                println("IOException: ${e.message}")
                            } catch (e: JSONException) {
                                println("JSONException: ${e.message}")
                            } catch (e: Exception) {
                                println("Exception: $e")
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Konum alınamadığında yapılacak işlemler
                        println("Konum alınamadı: ${exception.message}")
                    }
            }
        }
    }

}