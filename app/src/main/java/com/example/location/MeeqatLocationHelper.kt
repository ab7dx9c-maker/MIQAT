package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.example.data.local.AdhkarCatalog
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "MeeqatLocationHelper"

data class DetectedLocation(
    val cityArabic: String,
    val cityEnglish: String,
    val countryArabic: String,
    val countryEnglish: String,
    val latitude: Double,
    val longitude: Double
)

class MeeqatLocationHelper(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun fetchCoarseLocation(): DetectedLocation? = withContext(Dispatchers.IO) {
        try {
            val location = withTimeoutOrNull(8_000L) {
                suspendCancellableCoroutine<android.location.Location?> { cont ->
                    val tokenSource = CancellationTokenSource()
                    cont.invokeOnCancellation { tokenSource.cancel() }
                    fusedClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        tokenSource.token
                    ).addOnSuccessListener { loc ->
                        if (loc != null) {
                            cont.resume(loc)
                        } else {
                            fusedClient.lastLocation.addOnSuccessListener { last ->
                                cont.resume(last)
                            }.addOnFailureListener { cont.resume(null) }
                        }
                    }.addOnFailureListener {
                        fusedClient.lastLocation.addOnSuccessListener { last ->
                            cont.resume(last)
                        }.addOnFailureListener { cont.resume(null) }
                    }
                }
            } ?: return@withContext null

            val ar = resolveCityCountry(location.latitude, location.longitude, Locale("ar"))
            val en = resolveCityCountry(location.latitude, location.longitude, Locale.ENGLISH)
            DetectedLocation(
                cityArabic = ar?.first ?: en?.first ?: "موقعك الحالي",
                cityEnglish = en?.first ?: ar?.first ?: "Current Location",
                countryArabic = ar?.second ?: en?.second ?: "",
                countryEnglish = en?.second ?: ar?.second ?: "",
                latitude = location.latitude,
                longitude = location.longitude
            )
        } catch (e: Exception) {
            Log.w(TAG, "Unable to fetch GPS location: ${e.message}")
            null
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveCityCountry(
        lat: Double,
        lon: Double,
        locale: Locale
    ): Pair<String, String>? {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, locale)
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lon, 1) { list ->
                            cont.resume(list)
                        }
                    }
                } else {
                    geocoder.getFromLocation(lat, lon, 1)
                }
                val first = addresses?.firstOrNull()
                if (first != null) {
                    val city = first.locality ?: first.subAdminArea ?: first.adminArea
                    val country = first.countryName
                    if (!city.isNullOrBlank()) {
                        return city to (country ?: "")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder lookup failed: ${e.message}")
        }

        val nearest = AdhkarCatalog.fallbackCities.minByOrNull { city ->
            sqrt((city.latitude - lat).pow(2) + (city.longitude - lon).pow(2))
        } ?: return null

        return if (locale.language == "en") {
            nearest.englishName to nearest.countryEnglish
        } else {
            nearest.arabicName to nearest.countryArabic
        }
    }
}
