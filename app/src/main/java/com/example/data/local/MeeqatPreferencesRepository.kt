package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.ArabicFontStyle
import com.example.data.model.AzanPreset
import com.example.data.model.CustomAdhkarAudio
import com.example.data.model.MeeqatThemeMode
import com.example.data.model.PrayerKey
import com.example.data.model.ReciterVoiceProfile
import com.example.data.model.TasbihGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.meeqatDataStore: DataStore<Preferences> by preferencesDataStore(name = "meeqat_prefs")

data class MeeqatUserPrefs(
    val themeMode: MeeqatThemeMode = MeeqatThemeMode.PEARL_WHITE,
    val fontStyle: ArabicFontStyle = ArabicFontStyle.RUQAA,
    val language: AppLanguage = AppLanguage.ARABIC,
    val masterAlertsEnabled: Boolean = true,
    val mutedPrayers: Set<PrayerKey> = emptySet(),
    val iqamahMinutes: Int = 15,
    val selectedAzanPreset: AzanPreset = AzanPreset.HIJAZ_SYNTH,
    val customAzanUri: String? = null,
    val customAzanName: String? = null,
    val cityName: String = "مكة المكرمة",
    val cityNameEn: String = "Makkah",
    val countryName: String = "السعودية",
    val countryNameEn: String = "Saudi Arabia",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val isUsingGps: Boolean = true,
    val tasbihDhikrId: String = "subhan",
    val tasbihCount: Int = 0,
    val tasbihTotalCount: Int = 0,
    val tasbihGoal: TasbihGoal = TasbihGoal.THIRTY_THREE,
    val reciterProfile: ReciterVoiceProfile = ReciterVoiceProfile.CALM_KHASHIA,
    val reciterSystemVoiceName: String? = null,
    val customAdhkarAudios: Map<String, CustomAdhkarAudio> = emptyMap(),
    val comfortableReadingMode: Boolean = false
) {
    fun displayCity(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) cityNameEn else cityName

    fun displayCountry(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) countryNameEn else countryName
}

class MeeqatPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val MASTER_ALERTS = booleanPreferencesKey("master_alerts")
        val MUTED_PRAYERS = stringSetPreferencesKey("muted_prayers")
        val IQAMAH_MINUTES = intPreferencesKey("iqamah_minutes")
        val AZAN_PRESET = stringPreferencesKey("azan_preset")
        val CUSTOM_AZAN_URI = stringPreferencesKey("custom_azan_uri")
        val CUSTOM_AZAN_NAME = stringPreferencesKey("custom_azan_name")
        val CITY_NAME = stringPreferencesKey("city_name")
        val CITY_NAME_EN = stringPreferencesKey("city_name_en")
        val COUNTRY_NAME = stringPreferencesKey("country_name")
        val COUNTRY_NAME_EN = stringPreferencesKey("country_name_en")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val USING_GPS = booleanPreferencesKey("using_gps")
        val TASBIH_DHIKR_ID = stringPreferencesKey("tasbih_dhikr_id")
        val TASBIH_COUNT = intPreferencesKey("tasbih_count")
        val TASBIH_TOTAL_COUNT = intPreferencesKey("tasbih_total_count")
        val TASBIH_GOAL = stringPreferencesKey("tasbih_goal")
        val RECITER_PROFILE = stringPreferencesKey("reciter_profile")
        val RECITER_VOICE_NAME = stringPreferencesKey("reciter_voice_name")
        val CUSTOM_ADHKAR_AUDIOS = stringSetPreferencesKey("custom_adhkar_audios")
        val COMFORTABLE_READING_MODE = booleanPreferencesKey("comfortable_reading_mode")
    }

    val prefsFlow: Flow<MeeqatUserPrefs> = context.meeqatDataStore.data.map { p ->
        val mutedSet = (p[Keys.MUTED_PRAYERS] ?: emptySet())
            .mapNotNull { name -> PrayerKey.entries.firstOrNull { it.name == name } }
            .toSet()
        val goal = TasbihGoal.entries.firstOrNull { it.name == p[Keys.TASBIH_GOAL] }
            ?: TasbihGoal.THIRTY_THREE

        val customAdhkarMap = (p[Keys.CUSTOM_ADHKAR_AUDIOS] ?: emptySet())
            .mapNotNull { entry ->
                val parts = entry.split("|||", limit = 3)
                if (parts.size == 3) {
                    parts[0] to CustomAdhkarAudio(uri = parts[1], fileName = parts[2])
                } else null
            }
            .toMap()

        MeeqatUserPrefs(
            themeMode = MeeqatThemeMode.fromId(p[Keys.THEME_MODE]),
            fontStyle = ArabicFontStyle.fromId(p[Keys.FONT_STYLE]),
            language = AppLanguage.fromId(p[Keys.APP_LANGUAGE]),
            masterAlertsEnabled = p[Keys.MASTER_ALERTS] ?: true,
            mutedPrayers = mutedSet,
            iqamahMinutes = (p[Keys.IQAMAH_MINUTES] ?: 15).coerceIn(5, 30),
            selectedAzanPreset = AzanPreset.fromId(p[Keys.AZAN_PRESET]),
            customAzanUri = p[Keys.CUSTOM_AZAN_URI],
            customAzanName = p[Keys.CUSTOM_AZAN_NAME],
            cityName = p[Keys.CITY_NAME] ?: "مكة المكرمة",
            cityNameEn = p[Keys.CITY_NAME_EN] ?: "Makkah",
            countryName = p[Keys.COUNTRY_NAME] ?: "السعودية",
            countryNameEn = p[Keys.COUNTRY_NAME_EN] ?: "Saudi Arabia",
            latitude = p[Keys.LATITUDE] ?: 21.4225,
            longitude = p[Keys.LONGITUDE] ?: 39.8262,
            isUsingGps = p[Keys.USING_GPS] ?: true,
            tasbihDhikrId = p[Keys.TASBIH_DHIKR_ID] ?: "subhan",
            tasbihCount = p[Keys.TASBIH_COUNT] ?: 0,
            tasbihTotalCount = p[Keys.TASBIH_TOTAL_COUNT] ?: 0,
            tasbihGoal = goal,
            reciterProfile = ReciterVoiceProfile.fromId(p[Keys.RECITER_PROFILE]),
            reciterSystemVoiceName = p[Keys.RECITER_VOICE_NAME],
            customAdhkarAudios = customAdhkarMap,
            comfortableReadingMode = p[Keys.COMFORTABLE_READING_MODE] ?: false
        )
    }

    suspend fun setThemeMode(mode: MeeqatThemeMode) {
        context.meeqatDataStore.edit { it[Keys.THEME_MODE] = mode.id }
    }

    suspend fun setFontStyle(style: ArabicFontStyle) {
        context.meeqatDataStore.edit { it[Keys.FONT_STYLE] = style.id }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.meeqatDataStore.edit { it[Keys.APP_LANGUAGE] = language.id }
    }

    suspend fun setComfortableReadingMode(enabled: Boolean) {
        context.meeqatDataStore.edit { it[Keys.COMFORTABLE_READING_MODE] = enabled }
    }

    suspend fun setMasterAlerts(enabled: Boolean) {
        context.meeqatDataStore.edit { it[Keys.MASTER_ALERTS] = enabled }
    }

    suspend fun togglePrayerMute(prayer: PrayerKey) {
        context.meeqatDataStore.edit { prefs ->
            val current = prefs[Keys.MUTED_PRAYERS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(prayer.name)) current.remove(prayer.name)
            else current.add(prayer.name)
            prefs[Keys.MUTED_PRAYERS] = current
        }
    }

    suspend fun setIqamahMinutes(minutes: Int) {
        context.meeqatDataStore.edit { it[Keys.IQAMAH_MINUTES] = minutes.coerceIn(5, 30) }
    }

    suspend fun setAzanPreset(preset: AzanPreset) {
        context.meeqatDataStore.edit { it[Keys.AZAN_PRESET] = preset.id }
    }

    suspend fun setCustomAzan(uri: String?, name: String?) {
        context.meeqatDataStore.edit { prefs ->
            if (uri != null) {
                prefs[Keys.CUSTOM_AZAN_URI] = uri
                prefs[Keys.CUSTOM_AZAN_NAME] = name ?: "ملف أذان مخصص"
            } else {
                prefs.remove(Keys.CUSTOM_AZAN_URI)
                prefs.remove(Keys.CUSTOM_AZAN_NAME)
            }
        }
    }

    suspend fun setLocation(
        city: String,
        cityEn: String,
        country: String,
        countryEn: String,
        latitude: Double,
        longitude: Double,
        isGps: Boolean
    ) {
        context.meeqatDataStore.edit { prefs ->
            prefs[Keys.CITY_NAME] = city
            prefs[Keys.CITY_NAME_EN] = cityEn
            prefs[Keys.COUNTRY_NAME] = country
            prefs[Keys.COUNTRY_NAME_EN] = countryEn
            prefs[Keys.LATITUDE] = latitude
            prefs[Keys.LONGITUDE] = longitude
            prefs[Keys.USING_GPS] = isGps
        }
    }

    suspend fun updateTasbih(dhikrId: String, count: Int, total: Int, goal: TasbihGoal) {
        context.meeqatDataStore.edit { prefs ->
            prefs[Keys.TASBIH_DHIKR_ID] = dhikrId
            prefs[Keys.TASBIH_COUNT] = count
            prefs[Keys.TASBIH_TOTAL_COUNT] = total
            prefs[Keys.TASBIH_GOAL] = goal.name
        }
    }

    suspend fun setReciterProfile(profile: ReciterVoiceProfile) {
        context.meeqatDataStore.edit { it[Keys.RECITER_PROFILE] = profile.id }
    }

    suspend fun setReciterSystemVoice(voiceName: String?) {
        context.meeqatDataStore.edit { prefs ->
            if (voiceName != null) prefs[Keys.RECITER_VOICE_NAME] = voiceName
            else prefs.remove(Keys.RECITER_VOICE_NAME)
        }
    }

    suspend fun setCustomAdhkarAudio(dhikrId: String, uri: String?, fileName: String?) {
        context.meeqatDataStore.edit { prefs ->
            val current = (prefs[Keys.CUSTOM_ADHKAR_AUDIOS] ?: emptySet())
                .filterNot { it.startsWith("$dhikrId|||") }
                .toMutableSet()
            if (uri != null && fileName != null) {
                current.add("$dhikrId|||$uri|||$fileName")
            }
            prefs[Keys.CUSTOM_ADHKAR_AUDIOS] = current
        }
    }
}
