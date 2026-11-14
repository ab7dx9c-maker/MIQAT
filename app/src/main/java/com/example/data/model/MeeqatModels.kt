package com.example.data.model

enum class AppLanguage(val id: String, val nativeLabel: String) {
    ARABIC("ar", "العربية"),
    ENGLISH("en", "English");

    companion object {
        fun fromId(id: String?): AppLanguage =
            entries.firstOrNull { it.id == id } ?: ARABIC
    }
}

enum class MeeqatThemeMode(
    val id: String,
    val arabicName: String,
    val englishName: String
) {
    PEARL_WHITE("pearl_white", "الأبيض اللؤلؤي", "Pearl White"),
    ROYAL_BLACK("royal_black", "الأسود الملكي", "Royal Black");

    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishName else arabicName

    companion object {
        fun fromId(id: String?): MeeqatThemeMode =
            entries.firstOrNull { it.id == id } ?: PEARL_WHITE
    }
}

enum class MeeqatTab(val arabicTitle: String, val englishTitle: String) {
    PRAYERS("المواقيت", "Prayers"),
    QIBLA("القبلة", "Qibla"),
    TASBIH("السبحة", "Tasbih"),
    ADHKAR("الأذكار", "Adhkar"),
    QUIZ("أسئلة دينية", "Religious Quiz"),
    COMPANION("المرشد", "Guide");

    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishTitle else arabicTitle
}

enum class PrayerKey(
    val apiKey: String,
    val arabicName: String,
    val englishName: String,
    val hasIqamah: Boolean = true
) {
    FAJR("Fajr", "الفجر", "Fajr", true),
    SUNRISE("Sunrise", "الشروق", "Sunrise", false),
    DHUHR("Dhuhr", "الظهر", "Dhuhr", true),
    ASR("Asr", "العصر", "Asr", true),
    MAGHRIB("Maghrib", "المغرب", "Maghrib", true),
    ISHA("Isha", "العشاء", "Isha", true);

    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishName else arabicName
}

data class PrayerTimeItem(
    val key: PrayerKey,
    val azanTime24: String,
    val azanDisplayAr: String,
    val azanDisplayEn: String,
    val iqamahDisplayAr: String?,
    val iqamahDisplayEn: String?,
    val minutesFromMidnight: Int,
    val iqamahMinutesFromMidnight: Int?,
    val isNext: Boolean = false,
    val isMuted: Boolean = false
) {
    fun azanDisplay(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) azanDisplayEn else azanDisplayAr

    fun iqamahDisplay(lang: AppLanguage): String? =
        if (lang == AppLanguage.ENGLISH) iqamahDisplayEn else iqamahDisplayAr
}

data class HijriDateInfo(
    val day: String,
    val monthArabic: String,
    val monthEnglish: String,
    val year: String,
    val weekdayArabic: String,
    val weekdayEnglish: String
) {
    fun formatted(lang: AppLanguage): String = if (lang == AppLanguage.ENGLISH) {
        "$weekdayEnglish, $day $monthEnglish $year AH"
    } else {
        "$weekdayArabic $day $monthArabic $year هـ"
    }
}

data class DayPrayerSchedule(
    val prayers: List<PrayerTimeItem>,
    val hijriDate: HijriDateInfo,
    val gregorianArabicDate: String,
    val gregorianEnglishDate: String,
    val isFromApi: Boolean,
    val methodNameAr: String,
    val methodNameEn: String
) {
    fun gregorianDate(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) gregorianEnglishDate else gregorianArabicDate

    fun methodName(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) methodNameEn else methodNameAr
}

data class CityOption(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val countryArabic: String,
    val countryEnglish: String,
    val latitude: Double,
    val longitude: Double
) {
    fun name(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishName else arabicName

    fun country(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) countryEnglish else countryArabic
}

enum class AzanPreset(
    val id: String,
    val arabicTitle: String,
    val englishTitle: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val streamUrl: String?,
    val isSynthesizer: Boolean = false
) {
    HIJAZ_SYNTH(
        id = "hijaz_synth",
        arabicTitle = "توليف حجازي روحاني (مدمج)",
        englishTitle = "Built-in Hijaz Maqam Synth",
        subtitleAr = "يعمل دائماً بدون إنترنت بجودة صافية",
        subtitleEn = "Works offline reliably with serene harmonics",
        streamUrl = null,
        isSynthesizer = true
    ),
    MAKKAH(
        id = "makkah",
        arabicTitle = "أذان الحرم المكي",
        englishTitle = "Makkah Grand Mosque Azan",
        subtitleAr = "أذان المسجد الحرام بمكة المكرمة",
        subtitleEn = "Al-Masjid Al-Haram Call to Prayer",
        streamUrl = "https://www.islamcan.com/audio/adhan/azan1.mp3"
    ),
    MADINAH(
        id = "madinah",
        arabicTitle = "أذان المسجد النبوي",
        englishTitle = "Madinah Prophet's Mosque Azan",
        subtitleAr = "أذان الحرم النبوي الشريف بالمدينة المنورة",
        subtitleEn = "Al-Masjid An-Nabawi Call to Prayer",
        streamUrl = "https://www.islamcan.com/audio/adhan/azan2.mp3"
    ),
    AFASY(
        id = "afasy",
        arabicTitle = "مشاري راشد العفاسي",
        englishTitle = "Mishary Rashid Alafasy",
        subtitleAr = "تلاوة أذان خاشعة بصوت الشيخ العفاسي",
        subtitleEn = "Melodic Azan recitation by Sheikh Alafasy",
        streamUrl = "https://www.islamcan.com/audio/adhan/azan3.mp3"
    );

    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishTitle else arabicTitle

    fun subtitle(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) subtitleEn else subtitleAr

    companion object {
        fun fromId(id: String?): AzanPreset =
            entries.firstOrNull { it.id == id } ?: HIJAZ_SYNTH
    }
}

data class DhikrPreset(
    val id: String,
    val arabicText: String,
    val englishText: String,
    val noteAr: String,
    val noteEn: String
) {
    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishText else arabicText

    fun note(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) noteEn else noteAr
}

enum class TasbihGoal(val target: Int?, val labelAr: String, val labelEn: String) {
    THIRTY_THREE(33, "33", "33"),
    ONE_HUNDRED(100, "100", "100"),
    UNLIMITED(null, "∞ مفتوح", "∞ Open");

    fun label(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) labelEn else labelAr
}

enum class AdhkarCategory(val arabicTitle: String, val englishTitle: String) {
    MORNING("أذكار الصباح", "Morning Adhkar"),
    EVENING("أذكار المساء", "Evening Adhkar"),
    POST_PRAYER("أذكار بعد الصلاة", "After Prayer");

    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishTitle else arabicTitle
}

data class AdhkarItem(
    val id: String,
    val category: AdhkarCategory,
    val arabicText: String,
    val englishTranslation: String,
    val referenceAr: String,
    val referenceEn: String,
    val benefitAr: String,
    val benefitEn: String,
    val initialCount: Int
) {
    fun reference(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) referenceEn else referenceAr

    fun benefit(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) benefitEn else benefitAr
}

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false
)

data class AlertBannerState(
    val id: Long,
    val title: String,
    val subtitle: String,
    val isIqamah: Boolean = false
)

data class CountdownState(
    val nextPrayerName: String,
    val nextPrayerKey: PrayerKey,
    val nextAzanDisplay: String,
    val formattedRemaining: String,
    val progress: Float
)

enum class ReciterVoiceProfile(
    val id: String,
    val arabicTitle: String,
    val englishTitle: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val pitch: Float,
    val speechRate: Float
) {
    CALM_KHASHIA(
        id = "calm_khashia",
        arabicTitle = "قارئ خاشع هادئ",
        englishTitle = "Calm Reverent",
        subtitleAr = "نبرة دافئة وإيقاع متأنٍ للتدبر",
        subtitleEn = "Warm tone and unhurried reflective pace",
        pitch = 0.88f,
        speechRate = 0.78f
    ),
    BALANCED_CLEAR(
        id = "balanced_clear",
        arabicTitle = "قارئ متوازن واضح",
        englishTitle = "Balanced Clear",
        subtitleAr = "نطق فصيح معتدل السرعة",
        subtitleEn = "Articulate pronunciation at natural speed",
        pitch = 1.00f,
        speechRate = 0.92f
    ),
    DEEP_MAJESTIC(
        id = "deep_majestic",
        arabicTitle = "قارئ رزين عميق",
        englishTitle = "Deep Serene",
        subtitleAr = "صوت رخيم مناسب لأذكار الصباح والمساء",
        subtitleEn = "Resonant tone ideal for morning & evening adhkar",
        pitch = 0.78f,
        speechRate = 0.84f
    ),
    FAST_REVIEW(
        id = "fast_review",
        arabicTitle = "قارئ سريع للمراجعة",
        englishTitle = "Swift Review",
        subtitleAr = "وتيرة أسرع لاستذكار الأذكار",
        subtitleEn = "Brisk pace for quick daily review",
        pitch = 1.04f,
        speechRate = 1.08f
    );

    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishTitle else arabicTitle

    fun subtitle(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) subtitleEn else subtitleAr

    companion object {
        fun fromId(id: String?): ReciterVoiceProfile =
            entries.firstOrNull { it.id == id } ?: CALM_KHASHIA
    }
}

data class CustomAdhkarAudio(
    val uri: String,
    val fileName: String
)

enum class ArabicFontStyle(
    val id: String,
    val arabicTitle: String,
    val englishTitle: String,
    val subtitle: String
) {
    RUQAA("ruqaa", "خط الرقعة الأصيل", "Ruq'ah Calligraphy", "خط عربي كلاسيكي أنيق (Aref Ruqaa)"),
    MODERN("modern", "الخط العصري", "Modern Clean", "خط حديث واضح (Cairo & Tajawal)");

    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishTitle else arabicTitle

    companion object {
        fun fromId(id: String?): ArabicFontStyle =
            entries.firstOrNull { it.id == id } ?: RUQAA
    }
}

enum class QuizDifficulty(
    val id: String,
    val arabicTitle: String,
    val englishTitle: String,
    val subtitleAr: String,
    val subtitleEn: String
) {
    EASY(
        id = "easy",
        arabicTitle = "سهل",
        englishTitle = "Easy",
        subtitleAr = "أساسيات العقيدة والأركان والقصص المألوفة",
        subtitleEn = "Foundational beliefs, pillars & well-known stories"
    ),
    MEDIUM(
        id = "medium",
        arabicTitle = "متوسط",
        englishTitle = "Medium",
        subtitleAr = "السيرة النبوية والفقه الميسر وأسباب النزول",
        subtitleEn = "Prophetic Seerah, everyday Fiqh & Qur'anic context"
    ),
    HARD(
        id = "hard",
        arabicTitle = "صعب",
        englishTitle = "Hard",
        subtitleAr = "مسائل دقيقة وعلوم الحديث والتاريخ الإسلامي",
        subtitleEn = "Scholarly details, Hadith sciences & deep history"
    );

    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishTitle else arabicTitle

    fun subtitle(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) subtitleEn else subtitleAr
}

enum class QuizTopic(
    val id: String,
    val arabicTitle: String,
    val englishTitle: String
) {
    HISTORY_THEOLOGY("history_theology", "التاريخ والعقيدة", "History & Theology"),
    THEOLOGY("theology", "العقيدة الإسلامية", "Islamic Theology"),
    HISTORY("history", "التاريخ الإسلامي", "Islamic History"),
    SEERAH("seerah", "السيرة النبوية", "Prophetic Seerah"),
    QURAN("quran", "القرآن الكريم", "Holy Qur'an"),
    FIQH("fiqh", "الفقه والعبادات", "Fiqh & Worship");

    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) englishTitle else arabicTitle
}

data class IslamicQuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val topicLabel: String
)

sealed interface QuizUiState {
    data object Idle : QuizUiState
    data object Loading : QuizUiState
    data class Ready(
        val questions: List<IslamicQuizQuestion>,
        val difficulty: QuizDifficulty,
        val topic: QuizTopic
    ) : QuizUiState
    data class Error(val message: String) : QuizUiState
}

