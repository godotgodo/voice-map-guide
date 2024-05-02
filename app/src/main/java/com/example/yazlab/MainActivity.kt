package com.example.yazlab

//import com.google.maps.model.DirectionsRoute

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.yazlab.ui.theme.YazlabTheme
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act)
        /* setContent {
            YazlabTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        } */

        val myButton: Button = findViewById(R.id.myButton)

        myButton.setOnClickListener {

            Thread {

                val apiKey = "AIzaSyCRS8WogACRzGRQaHwskr9fvWx-6TVZ04w"
                val origin = "40.7128,-74.0060" // Kullanıcının mevcut konumu
                val destination = "40.748817,-73.985428" // Kullanıcının gitmek istediği konum
                val mode = "walking" // Seyahat modu: yürüyüş

                val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=$origin" +
                        "&destination=$destination" +
                        "&mode=$mode" +
                        "&key=$apiKey"

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()

                try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("Failed to get directions: $response")
                        val jsonResponse = JSONObject(response.body!!.string())
                        val gson = Gson()

                       // val routes = gson.fromJson(jsonResponse.getJSONArray("routes").toString(), Array<DirectionsRoute>::class.java)
                        val routes = jsonResponse.getJSONArray("routes")
                        if (routes.length() > 0) {
                            val firstRoute = routes.getJSONObject(0)
                            val legs = firstRoute.getJSONArray("legs")
                            for (i in 0 until legs.length()) {
                                val leg = legs.getJSONObject(i)
                                val steps = leg.getJSONArray("steps")
                                for (j in 0 until steps.length())
                                {
                                    val step = steps.getJSONObject(j)
                                    val distance = step.getJSONObject("distance")
                                    val meters = distance.get("value")
                                    try {
                                        val maneuver = step.get("maneuver")
                                        println("Step ${j + 1}:")
                                        println(meters.toString() + " metre sonra " + maneuver.toString())
                                    }
                                    catch (e: Exception){
                                        if(e is org.json.JSONException)
                                        {
                                            println("Step ${j + 1}:")
                                            println(meters.toString() + " metre boyunca devam edin")
                                            continue
                                        }
                                        continue
                                    }
                                }

                                /*GELEBİLECEK DEĞERLER MANEVRA İÇİN
                                *
                                * turn-slight-left, turn-sharp-left, turn-left, turn-slight-right, turn-sharp-right,
                                *  keep-right, keep-left, uturn-left, uturn-right, turn-right, straight, ramp-left,
                                * ramp-right, merge, fork-left, fork-right, ferry, ferry-train, roundabout-left, and roundabout-right
                                *  */

                               /* val distance = leg.getJSONObject("distance").getString("text")
                                val duration = leg.getJSONObject("duration").getString("text")
                                val startAddress = leg.getString("start_address")
                                val endAddress = leg.getString("end_address")
                                println("Distance: $distance, Duration: $duration")
                                println("Start Address: $startAddress")
                                println("End Address: $endAddress") */
                                // İlgili diğer bilgileri burada işleyebilirsiniz
                            }
                        } else {
                            println("No routes found")
                        }
                    }
                } catch (e: Exception) {
                    println(e)
                }
                runOnUiThread({
                    //Update UI
                })
            }.start()






        }

    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YazlabTheme {
        Greeting("Android")
    }
}

/*data class DirectionsRoute(
    @SerializedName("legs")
    val legs: Array<Leg>
) {
    data class Leg(
        @SerializedName("steps")
        val steps: Array<Step>
    ) {
        data class Step(
            @SerializedName("distzartretance")
            val zarttirit: String,
        )
    }
}*/