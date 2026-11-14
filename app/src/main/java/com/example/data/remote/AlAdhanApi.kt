package com.example.data.remote

import android.content.Context
import android.icu.util.IslamicCalendar
import android.os.Build
import android.util.Log
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.HijriDateInfo
import com.example.data.model.PrayerKey
import com.example.data.model.PrayerTimeItem
import com.example.util.NetworkStatusHelper
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

private const val TAG = "AlAdhanRepository"
private const val PREF_CACHE_NAME = "meeqat_prayer_cache"
private const val KEY_CACHE_ENTRY = "latest_schedule_json"

@JsonClass(generateAdapter = true)
data class AlAdhanResponse(
    val code: Int,
    val status: String,
    val data: AlAdhanData?
)

@JsonClass(generateAdapter = true)
data class AlAdhanData(
    val timings: AlAdhanTimings,
    val date: AlAdhanDate,
    val meta: AlAdhanMeta?
)

@JsonClass(generateAdapter = true)
data class AlAdhanTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String
)

@JsonClass(generateAdapter = true)
data class AlAdhanDate(
    val hijri: AlAdhanHijri
)

@JsonClass(generateAdapter = true)
data class AlAdhanHijri(
    val day: String,
    val month: AlAdhanHijriMonth,
    val year: String,
    val weekday: AlAdhanHijriWeekday
)

@JsonClass(generateAdapter = true)
data class AlAdhanHijriMonth(
    val number: Int,
    val ar: String,
    val en: String? = null
)

@JsonClass(generateAdapter = true)
data class AlAdhanHijriWeekday(
    val ar: String,
    val en: String? = null
)

@JsonClass(generateAdapter = true)
data class AlAdhanMeta(
    val method: AlAdhanMethod?
)

@JsonClass(generateAdapter = true)
data class AlAdhanMethod(
    val name: String?
)

interface AlAdhanService {
    @GET("v1/timings/{date}")
    suspend fun getTimings(
        @Path("date") dateDdMmYyyy: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 4
    ): AlAdhanResponse
}

class PrayerRepository(private val context: Context) {
    private val cachePrefs by lazy {
        context.applicationContext.getSharedPreferences(PREF_CACHE_NAME, Context.MODE_PRIVATE)
    }

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    private val api: AlAdhanService = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(AlAdhanService::class.java)

    suspend fun fetchPrayerSchedule(
        latitude: Double,
        longitude: Double,
        iqamahMinutes: Int,
        mutedPrayers: Set<PrayerKey>
    ): DayPrayerSchedule {
        val now = Calendar.getInstance()
        val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(now.time)
        val gregAr = SimpleDateFormat("d MMMM yyyy م", Locale("ar")).format(now.time)
        val gregEn = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(now.time)
        val cacheKey = buildCacheKey(dateStr, latitude, longitude)

        // Instant offline path: skip network timeout completely when offline
        if (!NetworkStatusHelper.isOnline(context)) {
            loadCachedSchedule(cacheKey, iqamahMinutes, mutedPrayers, gregAr, gregEn)?.let {
                return it
            }
            return buildOfflineAstronomicalSchedule(
                latitude = latitude,
                longitude = longitude,
                now = now,
                iqamahMinutes = iqamahMinutes,
                mutedPrayers = mutedPrayers,
                gregAr = gregAr,
                gregEn = gregEn
            )
        }

        return try {
            val response = api.getTimings(
                dateDdMmYyyy = dateStr,
                latitude = latitude,
                longitude = longitude,
                method = 4
            )
            val data = response.data ?: throw IllegalStateException("Empty AlAdhan payload")
            val rawMap = mapOf(
                PrayerKey.FAJR to cleanTime(data.timings.fajr),
                PrayerKey.SUNRISE to cleanTime(data.timings.sunrise),
                PrayerKey.DHUHR to cleanTime(data.timings.dhuhr),
                PrayerKey.ASR to cleanTime(data.timings.asr),
                PrayerKey.MAGHRIB to cleanTime(data.timings.maghrib),
                PrayerKey.ISHA to cleanTime(data.timings.isha)
            )
            val items = buildPrayerItems(rawMap, iqamahMinutes, mutedPrayers)
            val monthIndex = (data.date.hijri.month.number - 1).coerceIn(0, 11)
            val hijri = HijriDateInfo(
                day = data.date.hijri.day,
                monthArabic = data.date.hijri.month.ar,
                monthEnglish = data.date.hijri.month.en ?: englishHijriMonths[monthIndex],
                year = data.date.hijri.year,
                weekdayArabic = data.date.hijri.weekday.ar,
                weekdayEnglish = data.date.hijri.weekday.en ?: SimpleDateFormat("EEEE", Locale.ENGLISH).format(now.time)
            )
            val methodAr = data.meta?.method?.name ?: "أم القرى"
            val methodEn = data.meta?.method?.name ?: "Umm Al-Qura"
            saveCachedSchedule(cacheKey, rawMap, hijri, methodAr, methodEn)
            DayPrayerSchedule(
                prayers = items,
                hijriDate = hijri,
                gregorianArabicDate = gregAr,
                gregorianEnglishDate = gregEn,
                isFromApi = true,
                methodNameAr = methodAr,
                methodNameEn = methodEn
            )
        } catch (e: Exception) {
            Log.w(TAG, "API unavailable, using cached/offline calculation: ${e.message}")
            loadCachedSchedule(cacheKey, iqamahMinutes, mutedPrayers, gregAr, gregEn)
                ?: buildOfflineAstronomicalSchedule(
                    latitude = latitude,
                    longitude = longitude,
                    now = now,
                    iqamahMinutes = iqamahMinutes,
                    mutedPrayers = mutedPrayers,
                    gregAr = gregAr,
                    gregEn = gregEn
                )
        }
    }

    private fun buildOfflineAstronomicalSchedule(
        latitude: Double,
        longitude: Double,
        now: Calendar,
        iqamahMinutes: Int,
        mutedPrayers: Set<PrayerKey>,
        gregAr: String,
        gregEn: String
    ): DayPrayerSchedule {
        val fallbackTimes = calculateOfflineTimes(latitude, longitude, now)
        val items = buildPrayerItems(fallbackTimes, iqamahMinutes, mutedPrayers)
        return DayPrayerSchedule(
            prayers = items,
            hijriDate = getLocalHijriDate(now),
            gregorianArabicDate = gregAr,
            gregorianEnglishDate = gregEn,
            isFromApi = false,
            methodNameAr = "حساب فلكي محلي (بدون إنترنت)",
            methodNameEn = "Local Astronomical (Offline)"
        )
    }

    private fun buildCacheKey(dateStr: String, lat: Double, lon: Double): String =
        "$dateStr|${String.format(Locale.US, "%.2f,%.2f", lat, lon)}"

    private fun saveCachedSchedule(
        cacheKey: String,
        rawMap: Map<PrayerKey, String>,
        hijri: HijriDateInfo,
        methodAr: String,
        methodEn: String
    ) {
        try {
            val timingsJson = JSONObject().apply {
                rawMap.forEach { (key, value) -> put(key.name, value) }
            }
            val hijriJson = JSONObject().apply {
                put("day", hijri.day)
                put("monthArabic", hijri.monthArabic)
                put("monthEnglish", hijri.monthEnglish)
                put("year", hijri.year)
                put("weekdayArabic", hijri.weekdayArabic)
                put("weekdayEnglish", hijri.weekdayEnglish)
            }
            val root = JSONObject().apply {
                put("key", cacheKey)
                put("timings", timingsJson)
                put("hijri", hijriJson)
                put("methodAr", methodAr)
                put("methodEn", methodEn)
            }
            cachePrefs.edit().putString(KEY_CACHE_ENTRY, root.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving prayer cache: ${e.message}")
        }
    }

    private fun loadCachedSchedule(
        cacheKey: String,
        iqamahMinutes: Int,
        mutedPrayers: Set<PrayerKey>,
        gregAr: String,
        gregEn: String
    ): DayPrayerSchedule? {
        return try {
            val json = cachePrefs.getString(KEY_CACHE_ENTRY, null) ?: return null
            val root = JSONObject(json)
            if (root.optString("key") != cacheKey) return null
            val timingsObj = root.getJSONObject("timings")
            val rawMap = PrayerKey.entries.associateWith { key ->
                timingsObj.optString(key.name).ifBlank { return null }
            }
            val hijriObj = root.getJSONObject("hijri")
            val hijri = HijriDateInfo(
                day = hijriObj.optString("day"),
                monthArabic = hijriObj.optString("monthArabic"),
                monthEnglish = hijriObj.optString("monthEnglish"),
                year = hijriObj.optString("year"),
                weekdayArabic = hijriObj.optString("weekdayArabic"),
                weekdayEnglish = hijriObj.optString("weekdayEnglish")
            )
            DayPrayerSchedule(
                prayers = buildPrayerItems(rawMap, iqamahMinutes, mutedPrayers),
                hijriDate = hijri,
                gregorianArabicDate = gregAr,
                gregorianEnglishDate = gregEn,
                isFromApi = false,
                methodNameAr = "محفوظ محلياً (بدون إنترنت)",
                methodNameEn = "Cached Locally (Offline)"
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading prayer cache: ${e.message}")
            null
        }
    }

    fun rebuildWithSettings(
        schedule: DayPrayerSchedule,
        iqamahMinutes: Int,
        mutedPrayers: Set<PrayerKey>
    ): DayPrayerSchedule {
        val rawMap = schedule.prayers.associate { it.key to it.azanTime24 }
        return schedule.copy(
            prayers = buildPrayerItems(rawMap, iqamahMinutes, mutedPrayers)
        )
    }

    private fun cleanTime(raw: String): String {
        val clean = raw.substringBefore(" ").trim()
        val parts = clean.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return String.format(Locale.US, "%02d:%02d", h, m)
    }

    private fun buildPrayerItems(
        rawTimes: Map<PrayerKey, String>,
        iqamahMinutes: Int,
        mutedPrayers: Set<PrayerKey>
    ): List<PrayerTimeItem> {
        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val rawItems = PrayerKey.entries.map { key ->
            val time24 = rawTimes[key] ?: "00:00"
            val parts = time24.split(":")
            val mins = (parts[0].toInt() * 60) + parts[1].toInt()
            val iqamahMins = if (key.hasIqamah) (mins + iqamahMinutes) % (24 * 60) else null
            PrayerTimeItem(
                key = key,
                azanTime24 = time24,
                azanDisplayAr = format12Hour(mins, arabic = true),
                azanDisplayEn = format12Hour(mins, arabic = false),
                iqamahDisplayAr = iqamahMins?.let { format12Hour(it, arabic = true) },
                iqamahDisplayEn = iqamahMins?.let { format12Hour(it, arabic = false) },
                minutesFromMidnight = mins,
                iqamahMinutesFromMidnight = iqamahMins,
                isNext = false,
                isMuted = mutedPrayers.contains(key)
            )
        }
        val nextKey = rawItems.firstOrNull { it.minutesFromMidnight > nowMinutes }?.key
            ?: PrayerKey.FAJR
        return rawItems.map { it.copy(isNext = it.key == nextKey) }
    }

    private fun format12Hour(totalMinutes: Int, arabic: Boolean): String {
        val h24 = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        val suffix = if (arabic) {
            if (h24 < 12) "ص" else "م"
        } else {
            if (h24 < 12) "AM" else "PM"
        }
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return String.format(Locale.US, "%02d:%02d %s", h12, m, suffix)
    }

    private fun getLocalHijriDate(cal: Calendar): HijriDateInfo {
        val arabicMonths = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        val arabicWeekdays = mapOf(
            Calendar.SUNDAY to "الأحد",
            Calendar.MONDAY to "الإثنين",
            Calendar.TUESDAY to "الثلاثاء",
            Calendar.WEDNESDAY to "الأربعاء",
            Calendar.THURSDAY to "الخميس",
            Calendar.FRIDAY to "الجمعة",
            Calendar.SATURDAY to "السبت"
        )
        val weekdayAr = arabicWeekdays[cal.get(Calendar.DAY_OF_WEEK)] ?: ""
        val weekdayEn = SimpleDateFormat("EEEE", Locale.ENGLISH).format(cal.time)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val islamic = IslamicCalendar(cal.time)
            val day = islamic.get(IslamicCalendar.DAY_OF_MONTH).toString()
            val monthIndex = islamic.get(IslamicCalendar.MONTH).coerceIn(0, 11)
            val year = islamic.get(IslamicCalendar.YEAR).toString()
            HijriDateInfo(
                day = day,
                monthArabic = arabicMonths[monthIndex],
                monthEnglish = englishHijriMonths[monthIndex],
                year = year,
                weekdayArabic = weekdayAr,
                weekdayEnglish = weekdayEn
            )
        } else {
            HijriDateInfo("1", "رمضان", "Ramadan", "1448", weekdayAr, weekdayEn)
        }
    }

    private fun calculateOfflineTimes(
        lat: Double,
        lon: Double,
        cal: Calendar
    ): Map<PrayerKey, String> {
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val tzOffsetHours = TimeZone.getDefault().getOffset(cal.timeInMillis) / 3600000.0
        val b = (2.0 * PI / 365.0) * (dayOfYear - 81)
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b)
        val declDeg = 23.45 * sin(b)
        val decl = Math.toRadians(declDeg)
        val latRad = Math.toRadians(lat)

        val solarNoonHours = 12.0 + tzOffsetHours - (lon / 15.0) - (eot / 60.0)

        fun hourAngleForAltitude(altDeg: Double): Double {
            val altRad = Math.toRadians(altDeg)
            val cosHa = (sin(altRad) - sin(latRad) * sin(decl)) / (cos(latRad) * cos(decl))
            val clamped = cosHa.coerceIn(-0.999, 0.999)
            return Math.toDegrees(acos(clamped)) / 15.0
        }

        val sunriseHa = hourAngleForAltitude(-0.833)
        val fajrHa = hourAngleForAltitude(-18.5)
        val asrAltDeg = Math.toDegrees(atan2(1.0, 1.0 + tan(kotlin.math.abs(latRad - decl))))
        val asrHa = hourAngleForAltitude(asrAltDeg)
        val ishaHours = solarNoonHours + sunriseHa + 1.5

        fun hoursTo24(hours: Double): String {
            val totalMins = ((hours * 60.0).toInt() % (24 * 60) + (24 * 60)) % (24 * 60)
            return String.format(Locale.US, "%02d:%02d", totalMins / 60, totalMins % 60)
        }

        return mapOf(
            PrayerKey.FAJR to hoursTo24(solarNoonHours - fajrHa),
            PrayerKey.SUNRISE to hoursTo24(solarNoonHours - sunriseHa),
            PrayerKey.DHUHR to hoursTo24(solarNoonHours + 0.03),
            PrayerKey.ASR to hoursTo24(solarNoonHours + asrHa),
            PrayerKey.MAGHRIB to hoursTo24(solarNoonHours + sunriseHa),
            PrayerKey.ISHA to hoursTo24(floor(ishaHours * 60.0) / 60.0)
        )
    }

    companion object {
        private val englishHijriMonths = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
    }
}
