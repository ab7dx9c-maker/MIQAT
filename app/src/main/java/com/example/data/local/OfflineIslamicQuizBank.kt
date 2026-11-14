package com.example.data.local

import com.example.data.model.AppLanguage
import com.example.data.model.IslamicQuizQuestion
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizTopic

private data class LocalQuizEntry(
    val id: String,
    val difficulty: QuizDifficulty,
    val topic: QuizTopic,
    val questionAr: String,
    val optionsAr: List<String>,
    val explanationAr: String,
    val topicLabelAr: String,
    val questionEn: String,
    val optionsEn: List<String>,
    val explanationEn: String,
    val topicLabelEn: String,
    val correctIndex: Int
) {
    fun toModel(language: AppLanguage, stamp: Long, idx: Int): IslamicQuizQuestion {
        return if (language == AppLanguage.ENGLISH) {
            IslamicQuizQuestion(
                id = "offline_${id}_${stamp}_$idx",
                question = questionEn,
                options = optionsEn,
                correctIndex = correctIndex,
                explanation = explanationEn,
                topicLabel = topicLabelEn
            )
        } else {
            IslamicQuizQuestion(
                id = "offline_${id}_${stamp}_$idx",
                question = questionAr,
                options = optionsAr,
                correctIndex = correctIndex,
                explanation = explanationAr,
                topicLabel = topicLabelAr
            )
        }
    }
}

object OfflineIslamicQuizBank {

    fun getQuestions(
        difficulty: QuizDifficulty,
        topic: QuizTopic,
        count: Int,
        language: AppLanguage
    ): List<IslamicQuizQuestion> {
        val exact = entries.filter { entry ->
            entry.difficulty == difficulty &&
                (topic == QuizTopic.HISTORY_THEOLOGY || entry.topic == topic)
        }.shuffled()

        val sameDifficulty = entries.filter { entry ->
            entry.difficulty == difficulty && entry !in exact
        }.shuffled()

        val remaining = entries.filter { entry ->
            entry !in exact && entry !in sameDifficulty
        }.shuffled()

        val selected = (exact + sameDifficulty + remaining).take(count)
        val stamp = System.currentTimeMillis()
        return selected.mapIndexed { idx, entry ->
            entry.toModel(language, stamp, idx)
        }
    }

    private val entries: List<LocalQuizEntry> = listOf(
        // ==================== EASY ====================
        LocalQuizEntry(
            id = "e_pillars_1",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.THEOLOGY,
            questionAr = "ما هو الركن الأول والأعظم من أركان الإسلام الخمسة؟",
            optionsAr = listOf(
                "شهادة أن لا إله إلا الله وأن محمداً رسول الله",
                "إيتاء الزكاة",
                "صوم رمضان",
                "حج البيت"
            ),
            explanationAr = "ثبت في الصحيحين حديث ابن عمر رضي الله عنهما: «بُني الإسلام على خمس: شهادة أن لا إله إلا الله وأن محمداً رسول الله...» وهو أساس التوحيد.",
            topicLabelAr = "العقيدة والأركان",
            questionEn = "What is the first and foundational pillar among the Five Pillars of Islam?",
            optionsEn = listOf(
                "Testifying that there is no deity worthy of worship except Allah and Muhammad is His Messenger",
                "Giving Zakah",
                "Fasting Ramadan",
                "Performing Hajj"
            ),
            explanationEn = "In Sahih al-Bukhari & Muslim, the Prophet ﷺ taught that Islam is built upon five pillars, beginning with the Shahadah (testimony of faith).",
            topicLabelEn = "Theology & Pillars",
            correctIndex = 0
        ),
        LocalQuizEntry(
            id = "e_iman_2",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.THEOLOGY,
            questionAr = "كم عدد أركان الإيمان التي ذكرها النبي ﷺ في حديث جبريل؟",
            optionsAr = listOf("خمسة أركان", "ستة أركان", "سبعة أركان", "أربعة أركان"),
            explanationAr = "أركان الإيمان ستة: الإيمان بالله، وملائكته، وكتبه، ورسله، واليوم الآخر، والقدر خيره وشره (رواه مسلم).",
            topicLabelAr = "العقيدة الإسلامية",
            questionEn = "How many Articles of Faith (Iman) did the Prophet ﷺ mention in the Hadith of Jibril?",
            optionsEn = listOf("Five articles", "Six articles", "Seven articles", "Four articles"),
            explanationEn = "In Sahih Muslim, Iman consists of six articles: belief in Allah, His angels, His books, His messengers, the Last Day, and Divine Decree (Qadr).",
            topicLabelEn = "Islamic Theology",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "e_seerah_3",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.SEERAH,
            questionAr = "في أي شهر هجري نزل القرآن الكريم وبدأت البعثة النبوية؟",
            optionsAr = listOf("رجب", "شعبان", "رمضان", "ذو الحجة"),
            explanationAr = "قال الله تعالى: ﴿شَهْرُ رَمَضَانَ الَّذِي أُنزِلَ فِيهِ الْقُرْآنُ﴾، وكان بدء نزول الوحي في غار حراء في رمضان.",
            topicLabelAr = "السيرة النبوية",
            questionEn = "In which Islamic month was the Holy Qur'an first revealed?",
            optionsEn = listOf("Rajab", "Sha'ban", "Ramadan", "Dhu al-Hijjah"),
            explanationEn = "Allah states in Surah Al-Baqarah (2:185): 'The month of Ramadan in which was revealed the Qur'an', beginning in the Cave of Hira.",
            topicLabelEn = "Prophetic Seerah",
            correctIndex = 2
        ),
        LocalQuizEntry(
            id = "e_history_4",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.HISTORY,
            questionAr = "من هو أول الخلفاء الراشدين بعد وفاة النبي محمد ﷺ؟",
            optionsAr = listOf(
                "عمر بن الخطاب رضي الله عنه",
                "عثمان بن عفان رضي الله عنه",
                "علي بن أبي طالب رضي الله عنه",
                "أبو بكر الصديق رضي الله عنه"
            ),
            explanationAr = "بويع أبو بكر الصديق رضي الله عنه بالخلافة في سقيفة بني ساعدة سنة 11 هـ، وهو رفيق النبي ﷺ في الهجرة.",
            topicLabelAr = "التاريخ الإسلامي",
            questionEn = "Who was the first Rightly Guided Caliph after the Prophet Muhammad ﷺ?",
            optionsEn = listOf(
                "Umar ibn al-Khattab",
                "Uthman ibn Affan",
                "Ali ibn Abi Talib",
                "Abu Bakr al-Siddiq"
            ),
            explanationEn = "Abu Bakr al-Siddiq (may Allah be pleased with him) was chosen as the first Caliph in 11 AH and was the Prophet's companion during the Hijrah.",
            topicLabelEn = "Islamic History",
            correctIndex = 3
        ),
        LocalQuizEntry(
            id = "e_quran_5",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.QURAN,
            questionAr = "ما هي السورة التي تُعرف بـ «قلب القرآن» أو تُسمى «أم الكتاب»؟ (المقصود أم الكتاب)",
            optionsAr = listOf("سورة البقرة", "سورة الفاتحة", "سورة الإخلاص", "سورة الكوثر"),
            explanationAr = "سورة الفاتحة هي «أم الكتاب» و«السبع المثاني» التي لا تصح الصلاة إلا بقراءتها (رواه البخاري).",
            topicLabelAr = "القرآن الكريم",
            questionEn = "Which Surah of the Qur'an is known as 'Umm al-Kitab' (Mother of the Book)?",
            optionsEn = listOf("Surah Al-Baqarah", "Surah Al-Fatihah", "Surah Al-Ikhlas", "Surah Al-Kawthar"),
            explanationEn = "Surah Al-Fatihah is called 'Umm al-Kitab' and 'As-Sab' al-Mathani' and is recited in every unit (rak'ah) of Salah.",
            topicLabelEn = "Holy Qur'an",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "e_fiqh_6",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.FIQH,
            questionAr = "كم عدد ركعات صلاة المغرب المفروضة؟",
            optionsAr = listOf("ركعتان", "ثلاث ركعات", "أربع ركعات", "خمس ركعات"),
            explanationAr = "صلاة المغرب ثلاث ركعات: ركعتان جهريتان ثم تشهد أوسط، ثم ركعة ثالثة سرية.",
            topicLabelAr = "الفقه والعبادات",
            questionEn = "How many obligatory units (rak'at) are in the Maghrib prayer?",
            optionsEn = listOf("Two rak'at", "Three rak'at", "Four rak'at", "Five rak'at"),
            explanationEn = "Maghrib consists of three obligatory rak'at: two recited aloud followed by the middle Tashahhud, then one silent rak'ah.",
            topicLabelEn = "Fiqh & Worship",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "e_seerah_7",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.SEERAH,
            questionAr = "إلى أي مدينة هاجر النبي ﷺ وأصحابه من مكة المكرمة؟",
            optionsAr = listOf("الطائف", "المدينة المنورة (يثرب)", "تبوك", "خيبر"),
            explanationAr = "هاجر النبي ﷺ من مكة إلى المدينة المنورة (التي كانت تُسمى يثرب)، ومنها بدأ التقويم الهجري.",
            topicLabelAr = "السيرة النبوية",
            questionEn = "To which city did the Prophet Muhammad ﷺ and his companions migrate from Makkah?",
            optionsEn = listOf("Ta'if", "Madinah (Yathrib)", "Tabuk", "Khaybar"),
            explanationEn = "The Hijrah to Madinah (formerly Yathrib) marked the founding of the first Islamic community and the start of the Hijri calendar.",
            topicLabelEn = "Prophetic Seerah",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "e_quran_8",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.QURAN,
            questionAr = "ما هي أطول سورة في القرآن الكريم؟",
            optionsAr = listOf("سورة آل عمران", "سورة النساء", "سورة البقرة", "سورة الأعراف"),
            explanationAr = "سورة البقرة هي أطول سور القرآن الكريم وتضم 286 آية، وفيها أعظم آية وهي آية الكرسي.",
            topicLabelAr = "القرآن الكريم",
            questionEn = "Which is the longest Surah in the Holy Qur'an?",
            optionsEn = listOf("Surah Aal-Imran", "Surah An-Nisa", "Surah Al-Baqarah", "Surah Al-A'raf"),
            explanationEn = "Surah Al-Baqarah is the longest chapter with 286 verses, and it contains Ayat al-Kursi (verse 255).",
            topicLabelEn = "Holy Qur'an",
            correctIndex = 2
        ),
        LocalQuizEntry(
            id = "e_fiqh_9",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.FIQH,
            questionAr = "ما اسم السورة القصيرة التي تعدل ثلث القرآن في الأجر؟",
            optionsAr = listOf("سورة الكوثر", "سورة الإخلاص", "سورة العصر", "سورة الفلق"),
            explanationAr = "صح عن النبي ﷺ أن سورة الإخلاص ﴿قُلْ هُوَ اللَّهُ أَحَدٌ﴾ تعدل ثلث القرآن لأنها خالصة في توحيد الله وصفاته.",
            topicLabelAr = "العقيدة والقرآن",
            questionEn = "Which short Surah is equal in reward to one-third of the Qur'an?",
            optionsEn = listOf("Surah Al-Kawthar", "Surah Al-Ikhlas", "Surah Al-Asr", "Surah Al-Falaq"),
            explanationEn = "In Sahih al-Bukhari & Muslim, Surah Al-Ikhlas (112) equals one-third of the Qur'an because it purely affirms Allah's Oneness.",
            topicLabelEn = "Theology & Qur'an",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "e_history_10",
            difficulty = QuizDifficulty.EASY,
            topic = QuizTopic.HISTORY,
            questionAr = "ما هو أول مسجد بُني في الإسلام في المدينة المنورة؟",
            optionsAr = listOf("المسجد الأقصى", "مسجد قباء", "مسجد القبلتين", "مسجد الخيف"),
            explanationAr = "أسس النبي ﷺ مسجد قباء فور وصوله إلى المدينة المنورة، وقد وصفه الله بأنه مسجد أُسس على التقوى من أول يوم.",
            topicLabelAr = "التاريخ الإسلامي",
            questionEn = "What was the first mosque built by the Prophet ﷺ upon arriving in Madinah?",
            optionsEn = listOf("Al-Aqsa Mosque", "Masjid Quba", "Masjid al-Qiblatayn", "Masjid al-Khayf"),
            explanationEn = "Masjid Quba was founded by the Prophet ﷺ on the outskirts of Madinah and is praised in the Qur'an as founded upon piety.",
            topicLabelEn = "Islamic History",
            correctIndex = 1
        ),

        // ==================== MEDIUM ====================
        LocalQuizEntry(
            id = "m_theology_1",
            difficulty = QuizDifficulty.MEDIUM,
            topic = QuizTopic.THEOLOGY,
            questionAr = "ما المقصود بمرتبة «الإحسان» كما عرّفها النبي ﷺ في حديث جبريل؟",
            optionsAr = listOf(
                "الإكثار من النوافل فقط",
                "أن تعبد الله كأنك تراه، فإن لم تكن تراه فإنه يراك",
                "إنفاق نصف المال في الصدقة",
                "حفظ المتون الفقهية"
            ),
            explanationAr = "أعلى مراتب الدين الثلاث (الإسلام، الإيمان، الإحسان) هو الإحسان: استشعار مراقبة الله وشهوده في كل عمل (رواه مسلم).",
            topicLabelAr = "العقيدة الإسلامية",
            questionEn = "How did the Prophet ﷺ define 'Ihsan' (spiritual excellence) in the Hadith of Jibril?",
            optionsEn = listOf(
                "Multiplying voluntary prayers alone",
                "To worship Allah as though you see Him, and though you do not see Him, He surely sees you",
                "Giving half of one's wealth in charity",
                "Memorizing legal treatises"
            ),
            explanationEn = "Ihsan is the highest of the three levels of religion (Islam, Iman, Ihsan), centered on constant mindfulness of Allah's watchfulness.",
            topicLabelEn = "Islamic Theology",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "m_history_2",
            difficulty = QuizDifficulty.MEDIUM,
            topic = QuizTopic.HISTORY,
            questionAr = "في عهد أي خليفة راشد جُمعت مصاحف القرآن على حرف واحد ووُزعت على الأمصار؟",
            optionsAr = listOf(
                "أبو بكر الصديق رضي الله عنه",
                "عمر بن الخطاب رضي الله عنه",
                "عثمان بن عفان رضي الله عنه",
                "علي بن أبي طالب رضي الله عنه"
            ),
            explanationAr = "أمر عثمان بن عفان رضي الله عنه لجنة برئاسة زيد بن ثابت بنسخ المصاحف وإرسالها إلى الأمصار سنة 25 هـ توحيداً للأمة على القراءة الثابتة.",
            topicLabelAr = "التاريخ الإسلامي",
            questionEn = "Under which Caliph were standardized copies of the Qur'an compiled and distributed to major Islamic provinces?",
            optionsEn = listOf(
                "Abu Bakr al-Siddiq",
                "Umar ibn al-Khattab",
                "Uthman ibn Affan",
                "Ali ibn Abi Talib"
            ),
            explanationEn = "Caliph Uthman ibn Affan commissioned a committee led by Zayd ibn Thabit around 25 AH to prepare and send authoritative copies (Mushaf).",
            topicLabelEn = "Islamic History",
            correctIndex = 2
        ),
        LocalQuizEntry(
            id = "m_seerah_3",
            difficulty = QuizDifficulty.MEDIUM,
            topic = QuizTopic.SEERAH,
            questionAr = "في أي سنة هجرية وقعت غزوة بدر الكبرى التي سماها القرآن «يوم الفرقان»؟",
            optionsAr = listOf("السنة الأولى للهجرة", "السنة الثانية للهجرة", "السنة الرابعة للهجرة", "السنة السادسة للهجرة"),
            explanationAr = "وقعت غزوة بدر الكبرى في 17 رمضان من السنة الثانية للهجرة، ونصر الله فيها المؤمنين نصراً مؤزراً.",
            topicLabelAr = "السيرة النبوية",
            questionEn = "In which Hijri year did the Battle of Badr ('Yawm al-Furqan') take place?",
            optionsEn = listOf("1 AH", "2 AH", "4 AH", "6 AH"),
            explanationEn = "The Battle of Badr occurred on 17 Ramadan in 2 AH, marking a decisive turning point in early Islamic history.",
            topicLabelEn = "Prophetic Seerah",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "m_seerah_4",
            difficulty = QuizDifficulty.MEDIUM,
            topic = QuizTopic.SEERAH,
            questionAr = "ما الصلح الذي أبرمه النبي ﷺ في السنة السادسة للهجرة وسماه الله «فتحاً مبيناً»؟",
            optionsAr = listOf("صلح الحديبية", "بيعة العقبة الأولى", "حلف الفضول", "فتح خيبر"),
            explanationAr = "عُقد صلح الحديبية في ذي القعدة سنة 6 هـ، ونزلت فيه سورة الفتح ﴿إِنَّا فَتَحْنَا لَكَ فَتْحًا مُّبِينًا﴾ لما ترتب عليه من انتشار الإسلام.",
            topicLabelAr = "السيرة النبوية",
            questionEn = "Which treaty in 6 AH did the Qur'an describe as 'a clear victory' (Fathan Mubina)?",
            optionsEn = listOf("Treaty of Hudaybiyyah", "First Pledge of Aqabah", "Hilf al-Fudul", "Conquest of Khaybar"),
            explanationEn = "The Treaty of Hudaybiyyah (6 AH) brought peace that enabled rapid spread of Islam, celebrated in Surah Al-Fath (48:1).",
            topicLabelEn = "Prophetic Seerah",
            correctIndex = 0
        ),
        LocalQuizEntry(
            id = "m_quran_5",
            difficulty = QuizDifficulty.MEDIUM,
            topic = QuizTopic.QURAN,
            questionAr = "ما هي السورة الوحيدة في القرآن الكريم التي لا تبدأ بالبسملة؟",
            optionsAr = listOf("سورة الأنفال", "سورة التوبة", "سورة النمل", "سورة الحشر"),
            explanationAr = "تبدأ جميع سور القرآن بالبسملة ما عدا سورة التوبة (براءة)، بينما وردت البسملة مرتين في سورة النمل.",
            topicLabelAr = "القرآن الكريم",
            questionEn = "Which is the only Surah in the Qur'an that does not begin with Bismillah?",
            optionsEn = listOf("Surah Al-Anfal", "Surah At-Tawbah", "Surah An-Naml", "Surah Al-Hashr"),
            explanationEn = "Surah At-Tawbah (Chapter 9) is the sole Surah without an opening Bismillah, whereas Surah An-Naml contains Bismillah twice.",
            topicLabelEn = "Holy Qur'an",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "m_fiqh_6",
            difficulty = QuizDifficulty.MEDIUM,
            topic = QuizTopic.FIQH,
            questionAr = "كم عدد الأصناف الثمانية المستحقة للزكاة المذكورة في سورة التوبة؟",
            optionsAr = listOf("ستة أصناف", "سبعة أصناف", "ثمانية أصناف", "عشرة أصناف"),
            explanationAr = "حددت الآية 60 من سورة التوبة ثمانية مصارف للزكاة: الفقراء، المساكين، العاملين عليها، المؤلفة قلوبهم، وفي الرقاب، والغارمين، وفي سبيل الله، وابن السبيل.",
            topicLabelAr = "الفقه والعبادات",
            questionEn = "How many categories of Zakah recipients are explicitly specified in Surah At-Tawbah (9:60)?",
            optionsEn = listOf("Six categories", "Seven categories", "Eight categories", "Ten categories"),
            explanationEn = "Surah At-Tawbah (9:60) specifies eight categories: the poor, the needy, Zakah administrators, those whose hearts are reconciled, freeing captives, those in debt, in the cause of Allah, and the stranded traveler.",
            topicLabelEn = "Fiqh & Worship",
            correctIndex = 2
        ),
        LocalQuizEntry(
            id = "m_theology_7",
            difficulty = QuizDifficulty.MEDIUM,
            topic = QuizTopic.THEOLOGY,
            questionAr = "ما المقصود بـ «توحيد الألوهية» عند علماء العقيدة؟",
            optionsAr = listOf(
                "الإقرار بأن الله هو الخالق الرازق فقط",
                "إفراد الله تعالى بجميع أنواع العبادة ظاهراً وباطناً",
                "معرفة التاريخ الإسلامي",
                "حفظ أسماء الصحابة"
            ),
            explanationAr = "توحيد الألوهية (العبادة) هو إفراد الله وحده بالعبادة كالدعاء والصلاة والنذر والتوكل، وهو خلاصة دعوة جميع الرسل.",
            topicLabelAr = "العقيدة الإسلامية",
            questionEn = "What does 'Tawhid al-Uluhiyyah' (Oneness of Worship) signify in Islamic theology?",
            optionsEn = listOf(
                "Merely acknowledging Allah as Creator",
                "Directing all acts of worship—outward and inward—exclusively to Allah alone",
                "Studying early Islamic chronicles",
                "Memorizing the names of the Companions"
            ),
            explanationEn = "Tawhid al-Uluhiyyah means dedicating all worship (prayer, supplication, reliance, devotion) solely to Allah, the core message of all Prophets.",
            topicLabelEn = "Islamic Theology",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "m_history_8",
            difficulty = QuizDifficulty.MEDIUM,
            topic = QuizTopic.HISTORY,
            questionAr = "من هو الخليفة الراشد الذي وضع التقويم الهجري وأنشأ الدواوين؟",
            optionsAr = listOf(
                "أبو بكر الصديق",
                "عمر بن الخطاب",
                "عثمان بن عفان",
                "معاوية بن أبي سفيان"
            ),
            explanationAr = "أسس عمر بن الخطاب رضي الله عنه التقويم الهجري مؤرخاً بالهجرة النبوية، وأنشأ ديوان الجند وبيت المال.",
            topicLabelAr = "التاريخ الإسلامي",
            questionEn = "Which Rightly Guided Caliph instituted the Hijri calendar and established administrative diwans?",
            optionsEn = listOf(
                "Abu Bakr al-Siddiq",
                "Umar ibn al-Khattab",
                "Uthman ibn Affan",
                "Mu'awiyah ibn Abi Sufyan"
            ),
            explanationEn = "Caliph Umar ibn al-Khattab established the Hijri calendar starting from the Prophet's migration and organized state registries (diwans).",
            topicLabelEn = "Islamic History",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "m_quran_9",
            difficulty = QuizDifficulty.MEDIUM,
            topic = QuizTopic.QURAN,
            questionAr = "أين تقع أعظم آية في القرآن الكريم «آية الكرسي»؟",
            optionsAr = listOf(
                "سورة آل عمران الآية 26",
                "سورة البقرة الآية 255",
                "سورة الحشر الآية 22",
                "سورة النور الآية 35"
            ),
            explanationAr = "آية الكرسي هي الآية 255 من سورة البقرة، وقد سأل النبي ﷺ أُبي بن كعب عن أعظم آية في كتاب الله فأجاب بها فأقره النبي ﷺ.",
            topicLabelAr = "القرآن الكريم",
            questionEn = "Where in the Qur'an is Ayat al-Kursi, praised by the Prophet ﷺ as the greatest verse, located?",
            optionsEn = listOf(
                "Surah Aal-Imran 3:26",
                "Surah Al-Baqarah 2:255",
                "Surah Al-Hashr 59:22",
                "Surah An-Nur 24:35"
            ),
            explanationEn = "In Sahih Muslim, Ubayy ibn Ka'b confirmed Ayat al-Kursi (Al-Baqarah 2:255) is the greatest verse in the Book of Allah.",
            topicLabelEn = "Holy Qur'an",
            correctIndex = 1
        ),

        // ==================== HARD ====================
        LocalQuizEntry(
            id = "h_history_1",
            difficulty = QuizDifficulty.HARD,
            topic = QuizTopic.HISTORY,
            questionAr = "في عهد أي خليفة عباسي بلغت «بيت الحكمة» في بغداد أوج ازدهارها العلمي والترجمي؟",
            optionsAr = listOf(
                "أبو جعفر المنصور",
                "هارون الرشيد",
                "عبد الله المأمون",
                "المتوكل على الله"
            ),
            explanationAr = "أسس الرشيد نواة بيت الحكمة، ثم وسّعه ابنه المأمون (حكم 198–218 هـ) ليصبح أعظم مركز عالمي للترجمة والبحث في الفلك والرياضيات والطب.",
            topicLabelAr = "الحضارة والتاريخ",
            questionEn = "Under which Abbasid Caliph did Bayt al-Hikmah (House of Wisdom) in Baghdad reach its zenith of scholarship and translation?",
            optionsEn = listOf(
                "Abu Ja'far al-Mansur",
                "Harun al-Rashid",
                "Abdullah al-Ma'mun",
                "Al-Mutawakkil"
            ),
            explanationEn = "While founded earlier under Harun al-Rashid, Caliph al-Ma'mun (r. 198–218 AH / 813–833 CE) transformed Bayt al-Hikmah into the foremost global center of scientific translation and research.",
            topicLabelEn = "Islamic History",
            correctIndex = 2
        ),
        LocalQuizEntry(
            id = "h_theology_2",
            difficulty = QuizDifficulty.HARD,
            topic = QuizTopic.THEOLOGY,
            questionAr = "ما هي الشروط الخمسة التي اتفق عليها علماء الحديث لصحة «الحديث الصحيح» لذاته؟",
            optionsAr = listOf(
                "اتصال السند، عدالة الرواة، تمام الضبط، عدم الشذوذ، وعدم العلة القادحة",
                "كثرة الرواة في كل طبقة دون نظر للضبط",
                "أن يكون مرفوعاً فقط دون التحقق من الاتصال",
                "موافقة القياس اللغوي فقط"
            ),
            explanationAr = "الحديث الصحيح عند المحدثين ما اتصل سنده بنقل العدل الضابط عن مثله إلى منتهاه من غير شذوذ ولا علة قادحة.",
            topicLabelAr = "علوم الحديث والعقيدة",
            questionEn = "Which five criteria must be met for a narration to be classified as 'Sahih li-dhatihi' (intrinsically authentic) in Hadith sciences?",
            optionsEn = listOf(
                "Continuous chain, uprightness of narrators, precision of memory, absence of anomaly (shudhudh), and absence of hidden defect ('illah)",
                "Large number of narrators without checking precision",
                "Being attributed to the Prophet ﷺ without verifying chain continuity",
                "Matching linguistic analogy alone"
            ),
            explanationEn = "Hadith scholars define Sahih as a narration with an unbroken chain of upright ('adl) and precise (dabit) narrators, free from shudhudh and damaging 'illah.",
            topicLabelEn = "Hadith & Theology",
            correctIndex = 0
        ),
        LocalQuizEntry(
            id = "h_history_3",
            difficulty = QuizDifficulty.HARD,
            topic = QuizTopic.HISTORY,
            questionAr = "من هو المؤرخ المسلم الذي كتب مقدمته الشهيرة «المقدمة» مؤسساً لعلم العمران البشري والاجتماع؟",
            optionsAr = listOf(
                "ابن الأثير الجزري",
                "عبد الرحمن بن خلدون",
                "محمد بن جرير الطبري",
                "شمس الدين الذهبي"
            ),
            explanationAr = "ألف العلامة عبد الرحمن بن خلدون (ت 808 هـ) كتاب العبر وابتدأه بـ «المقدمة» التي أرسى فيها قواعد نقد الأخبار وعلم الاجتماع والتاريخ.",
            topicLabelAr = "التاريخ الإسلامي",
            questionEn = "Which Muslim historian authored 'Al-Muqaddimah', laying the foundations of historiography and sociology?",
            optionsEn = listOf(
                "Ibn al-Athir",
                "Abd al-Rahman ibn Khaldun",
                "Muhammad ibn Jarir al-Tabari",
                "Shams al-Din al-Dhahabi"
            ),
            explanationEn = "Ibn Khaldun (d. 808 AH / 1406 CE) wrote Al-Muqaddimah as the introduction to Kitab al-Ibar, pioneering historical criticism and social science.",
            topicLabelEn = "Islamic History",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "h_theology_4",
            difficulty = QuizDifficulty.HARD,
            topic = QuizTopic.THEOLOGY,
            questionAr = "ما الفرق الأصولي الدقيق بين «الفرقان / المحكم» و«المتشابه» في القرآن الكريم كما ذكر في سورة آل عمران؟",
            optionsAr = listOf(
                "المحكم واضح الدلالة لا يحتمل إلا معنىً ظاهراً، والمتشابه يُردّ في فهمه إلى المحكم",
                "المحكم خاص بالأحكام المدنية والمتشابه بالقصص فقط",
                "المتشابه لا علاقة له بالقرآن",
                "المحكم هو المكي والمتشابه هو المدني"
            ),
            explanationAr = "في مطلع آل عمران ﴿مِنْهُ آيَاتٌ مُّحْكَمَاتٌ هُنَّ أُمُّ الْكِتَابِ وَأُخَرُ مُتَشَابِهَاتٌ﴾: الراسخون في العلم يردون المتشابه إلى المحكم البيّن.",
            topicLabelAr = "العقيدة والتفسير",
            questionEn = "According to Surah Aal-Imran (3:7), how do grounded scholars approach 'Muhkam' (clear) and 'Mutashabih' (ambiguous/allegorical) verses?",
            optionsEn = listOf(
                "Muhkam verses have clear, foundational meaning and Mutashabih verses are interpreted in light of the Muhkam",
                "Muhkam refers only to Madinan verses and Mutashabih to Makkan verses",
                "Mutashabih verses are disregarded in theology",
                "Muhkam refers strictly to historical narratives"
            ),
            explanationEn = "Surah Aal-Imran (3:7) describes Muhkam verses as 'Umm al-Kitab' (the foundation of the Book) by which Mutashabih verses are understood.",
            topicLabelEn = "Theology & Tafsir",
            correctIndex = 0
        ),
        LocalQuizEntry(
            id = "h_seerah_5",
            difficulty = QuizDifficulty.HARD,
            topic = QuizTopic.SEERAH,
            questionAr = "ما اسم الوثيقة الدستورية التي أبرمها النبي ﷺ فور استقراره في المدينة لتنظيم العلاقة بين المهاجرين والأنصار وسائر الطوائف؟",
            optionsAr = listOf(
                "صحيفة المدينة (دستور المدينة)",
                "وثيقة حنين",
                "كتاب تبوك",
                "ميثاق الطائف"
            ),
            explanationAr = "تُعد «صحيفة المدينة» أول وثيقة مدنية مكتوبة في الإسلام، أرست مبادئ المواطنة والتكافل والدفاع المشترك وحرية العقيدة ضمن السلم العام.",
            topicLabelAr = "السيرة النبوية",
            questionEn = "What is the historic constitutional charter drafted by the Prophet ﷺ in Madinah uniting Muhajirun, Ansar, and local tribes?",
            optionsEn = listOf(
                "Sahifat al-Madinah (Constitution of Madinah)",
                "Charter of Hunayn",
                "Edict of Tabuk",
                "Pact of Ta'if"
            ),
            explanationEn = "Sahifat al-Madinah (1 AH) established mutual security, justice, and civic responsibilities among Madinah's diverse communities.",
            topicLabelEn = "Prophetic Seerah",
            correctIndex = 0
        ),
        LocalQuizEntry(
            id = "h_fiqh_6",
            difficulty = QuizDifficulty.HARD,
            topic = QuizTopic.FIQH,
            questionAr = "كم عدد المقاصد الكلية الخمسة (الضروريات الخمس) التي اتفق علماء أصول الفقه على أن الشريعة جاءت لحفظها؟",
            optionsAr = listOf(
                "ثلاثة مقاصد",
                "خمسة مقاصد: الدين والنفس والعقل والنسل والمال",
                "أربعة مقاصد فقط",
                "سبعة مقاصد"
            ),
            explanationAr = "قرر الإمام الغزالي والشاطبي أن مقاصد الشريعة الضرورية خمسة: حفظ الدين، والنفس، والعقل، والنسل (العرض)، والمال.",
            topicLabelAr = "أصول الفقه",
            questionEn = "What are the five universal necessities (Al-Daruriyyat al-Khams) preserved by Islamic jurisprudence (Maqasid al-Shari'ah)?",
            optionsEn = listOf(
                "Three objectives",
                "Five: Faith (Din), Life (Nafs), Intellect ('Aql), Lineage (Nasl), and Wealth (Mal)",
                "Four objectives only",
                "Seven objectives"
            ),
            explanationEn = "Classical jurists such as al-Ghazali and al-Shatibi established five essential aims of Shari'ah: preserving faith, life, intellect, lineage/honor, and property.",
            topicLabelEn = "Usul al-Fiqh",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "h_quran_7",
            difficulty = QuizDifficulty.HARD,
            topic = QuizTopic.QURAN,
            questionAr = "من هو الصحابي الجليل الذي لقّبه النبي ﷺ بـ «ترجمان القرآن» ودعا له: «اللهم فقّهه في الدين وعلمه التأويل»؟",
            optionsAr = listOf(
                "عبد الله بن مسعود رضي الله عنه",
                "عبد الله بن عباس رضي الله عنهما",
                "أُبي بن كعب رضي الله عنه",
                "زيد بن ثابت رضي الله عنه"
            ),
            explanationAr = "دعا النبي ﷺ لابن عمه عبد الله بن عباس رضي الله عنهما بالفقه في الدين وعلم التأويل (التفسير)، فصار حبر الأمة وترجمان القرآن.",
            topicLabelAr = "علوم القرآن",
            questionEn = "Which Companion was titled 'Tarjuman al-Qur'an' (Interpreter of the Qur'an) after the Prophet ﷺ prayed for his deep understanding of Tafsir?",
            optionsEn = listOf(
                "Abdullah ibn Mas'ud",
                "Abdullah ibn Abbas",
                "Ubayy ibn Ka'b",
                "Zayd ibn Thabit"
            ),
            explanationEn = "The Prophet ﷺ supplicated for his young cousin Abdullah ibn Abbas: 'O Allah, grant him deep understanding of the religion and teach him interpretation.'",
            topicLabelEn = "Qur'anic Sciences",
            correctIndex = 1
        ),
        LocalQuizEntry(
            id = "h_history_8",
            difficulty = QuizDifficulty.HARD,
            topic = QuizTopic.HISTORY,
            questionAr = "في أي معركة تاريخية سنة 15 هـ انتهى الحكم الساساني في العراق بقيادة سعد بن أبي وقاص رضي الله عنه؟",
            optionsAr = listOf("معركة اليرموك", "معركة القادسية", "معركة ذات الصواري", "معركة عين جالوت"),
            explanationAr = "وقعت معركة القادسية سنة 15 هـ بقيادة سعد بن أبي وقاص رضي الله عنه وفتحت الطريق لفتح المدائن وانتشار الإسلام في بلاد فارس.",
            topicLabelAr = "التاريخ الإسلامي",
            questionEn = "Which decisive battle in 15 AH led by Sa'd ibn Abi Waqqas opened the way to Ctesiphon (Al-Mada'in) in Iraq?",
            optionsEn = listOf(
                "Battle of Yarmouk",
                "Battle of al-Qadisiyyah",
                "Battle of the Masts",
                "Battle of Ain Jalut"
            ),
            explanationEn = "The Battle of al-Qadisiyyah (15 AH) under Sa'd ibn Abi Waqqas was the decisive engagement in Iraq during Umar ibn al-Khattab's caliphate.",
            topicLabelEn = "Islamic History",
            correctIndex = 1
        )
    )
}
