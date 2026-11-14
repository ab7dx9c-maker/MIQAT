package com.example.data.local

import com.example.data.model.AdhkarCategory
import com.example.data.model.AdhkarItem
import com.example.data.model.CityOption
import com.example.data.model.DhikrPreset

object AdhkarCatalog {

    val fallbackCities: List<CityOption> = listOf(
        CityOption("makkah", "مكة المكرمة", "Makkah", "السعودية", "Saudi Arabia", 21.4225, 39.8262),
        CityOption("madinah", "المدينة المنورة", "Madinah", "السعودية", "Saudi Arabia", 24.4672, 39.6111),
        CityOption("muscat", "مسقط", "Muscat", "عُمان", "Oman", 23.5880, 58.3829),
        CityOption("shinas", "شناص", "Shinas", "عُمان", "Oman", 24.7426, 56.4669),
        CityOption("salalah", "صلالة", "Salalah", "عُمان", "Oman", 17.0151, 54.0924),
        CityOption("riyadh", "الرياض", "Riyadh", "السعودية", "Saudi Arabia", 24.7136, 46.6753),
        CityOption("jeddah", "جدة", "Jeddah", "السعودية", "Saudi Arabia", 21.4858, 39.1925),
        CityOption("dubai", "دبي", "Dubai", "الإمارات", "UAE", 25.2048, 55.2708),
        CityOption("abudhabi", "أبوظبي", "Abu Dhabi", "الإمارات", "UAE", 24.4539, 54.3773),
        CityOption("doha", "الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310),
        CityOption("kuwait", "الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774),
        CityOption("manama", "المنامة", "Manama", "البحرين", "Bahrain", 26.2285, 50.5860),
        CityOption("cairo", "القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357),
        CityOption("amman", "عمّان", "Amman", "الأردن", "Jordan", 31.9454, 35.9284),
        CityOption("jerusalem", "القدس", "Jerusalem", "فلسطين", "Palestine", 31.7683, 35.2137),
        CityOption("baghdad", "بغداد", "Baghdad", "العراق", "Iraq", 33.3152, 44.3661),
        CityOption("damascus", "دمشق", "Damascus", "سوريا", "Syria", 33.5138, 36.2765),
        CityOption("beirut", "بيروت", "Beirut", "لبنان", "Lebanon", 33.8938, 35.5018),
        CityOption("tunis", "تونس", "Tunis", "تونس", "Tunisia", 36.8065, 10.1815),
        CityOption("rabat", "الرباط", "Rabat", "المغرب", "Morocco", 34.0209, -6.8416),
        CityOption("istanbul", "إسطنبول", "Istanbul", "تركيا", "Turkey", 41.0082, 28.9784),
        CityOption("london", "لندن", "London", "بريطانيا", "United Kingdom", 51.5072, -0.1276)
    )

    val dhikrPresets: List<DhikrPreset> = listOf(
        DhikrPreset("subhan", "سبحان الله", "SubhanAllah", "تنزيه الله تعالى عن كل نقص", "Glory be to Allah, free from all imperfection"),
        DhikrPreset("hamd", "الحمد لله", "Alhamdulillah", "شكر الله وحمده على نعمه", "All praise and gratitude belong to Allah"),
        DhikrPreset("akbar", "الله أكبر", "Allahu Akbar", "تعظيم الله فوق كل شيء", "Allah is the Greatest above all things"),
        DhikrPreset("istighfar", "أستغفر الله", "Astaghfirullah", "طلب المغفرة والتوبة إلى الله", "Seeking forgiveness and turning to Allah"),
        DhikrPreset("tahlil", "لا إله إلا الله", "La ilaha illallah", "أفضل الذكر وكلمة التوحيد", "The finest remembrance and testimony of faith"),
        DhikrPreset("bihamdih", "سبحان الله وبحمده", "SubhanAllahi wa bihamdihi", "غُفرت خطاياه وإن كانت مثل زبد البحر", "Sins are forgiven even if like sea foam"),
        DhikrPreset("salawat", "اللهم صلِّ على محمد", "Allahumma salli 'ala Muhammad", "الصلاة على النبي ﷺ ترفع الدرجات", "Sending blessings upon the Prophet ﷺ elevates ranks")
    )

    val adhkarItems: List<AdhkarItem> = listOf(
        AdhkarItem(
            id = "m1",
            category = AdhkarCategory.MORNING,
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
            englishTranslation = "We have reached the morning and at this very time all sovereignty belongs to Allah. All praise is for Allah. None has the right to be worshipped except Allah alone, without partner; to Him belongs all sovereignty and praise, and He is over all things omnipotent.",
            referenceAr = "رواه مسلم",
            referenceEn = "Narrated by Muslim",
            benefitAr = "افتتاح اليوم بتوحيد الله وطلب خيره وبركته.",
            benefitEn = "Beginning the day with Tawhid and seeking Allah's blessing.",
            initialCount = 1
        ),
        AdhkarItem(
            id = "m2",
            category = AdhkarCategory.MORNING,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
            englishTranslation = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant, and I abide to Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for verily none can forgive sins except You.",
            referenceAr = "سيد الاستغفار — رواه البخاري",
            referenceEn = "Master Supplication for Forgiveness — Al-Bukhari",
            benefitAr = "من قالها موقناً بها حين يصبح فمات قبل أن يمسي فهو من أهل الجنة.",
            benefitEn = "Whoever recites it with conviction in the morning and dies before evening enters Paradise.",
            initialCount = 1
        ),
        AdhkarItem(
            id = "m3",
            category = AdhkarCategory.MORNING,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            englishTranslation = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.",
            referenceAr = "رواه أبو داود والترمذي",
            referenceEn = "Abu Dawud & At-Tirmidhi",
            benefitAr = "من قالها ثلاثاً لم يضره شيء بإذن الله.",
            benefitEn = "Recited three times, nothing shall harm the believer by Allah's permission.",
            initialCount = 3
        ),
        AdhkarItem(
            id = "m4",
            category = AdhkarCategory.MORNING,
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ.",
            englishTranslation = "Allah is sufficient for me; there is none worthy of worship but Him. Upon Him I rely, and He is the Lord of the Mighty Throne.",
            referenceAr = "رواه أبو داود",
            referenceEn = "Narrated by Abu Dawud",
            benefitAr = "من قالها حين يصبح وحين يمسي سبع مرات كفاه الله ما أهمه.",
            benefitEn = "Recited seven times morning and evening, Allah suffices all worries.",
            initialCount = 7
        ),
        AdhkarItem(
            id = "m5",
            category = AdhkarCategory.MORNING,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ.",
            englishTranslation = "Glory be to Allah and praise Him.",
            referenceAr = "رواه مسلم",
            referenceEn = "Narrated by Muslim",
            benefitAr = "من قالها مائة مرة حُطَّت خطاياه وإن كانت مثل زبد البحر.",
            benefitEn = "Recited 100 times, sins are wiped away even if like the foam of the sea.",
            initialCount = 100
        ),

        AdhkarItem(
            id = "e1",
            category = AdhkarCategory.EVENING,
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
            englishTranslation = "We have reached the evening and at this very time all sovereignty belongs to Allah. All praise is for Allah. None has the right to be worshipped except Allah alone, without partner; to Him belongs all sovereignty and praise, and He is over all things omnipotent.",
            referenceAr = "رواه مسلم",
            referenceEn = "Narrated by Muslim",
            benefitAr = "تجديد العهد مع الله في المساء وطلب خير الليلة.",
            benefitEn = "Renewing devotion to Allah at dusk and seeking the good of the night.",
            initialCount = 1
        ),
        AdhkarItem(
            id = "e2",
            category = AdhkarCategory.EVENING,
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
            englishTranslation = "I seek refuge in the perfect words of Allah from the evil of that which He has created.",
            referenceAr = "رواه مسلم",
            referenceEn = "Narrated by Muslim",
            benefitAr = "حصن وأمان في المساء من كل أذى.",
            benefitEn = "Protection and peace throughout the evening.",
            initialCount = 3
        ),
        AdhkarItem(
            id = "e3",
            category = AdhkarCategory.EVENING,
            arabicText = "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ وَأَنَّ مُحَمَّداً عَبْدُكَ وَرَسُولُكَ.",
            englishTranslation = "O Allah, verily I have reached the evening and call on You, the bearers of Your Throne, Your angels, and all of Your creation to witness that You are Allah, none has the right to be worshipped except You alone, without partner, and that Muhammad is Your servant and Messenger.",
            referenceAr = "رواه أبو داود",
            referenceEn = "Narrated by Abu Dawud",
            benefitAr = "من قالها أربع مرات أعتقه الله من النار.",
            benefitEn = "Whoever recites it four times is granted protection from the Fire.",
            initialCount = 4
        ),
        AdhkarItem(
            id = "e4",
            category = AdhkarCategory.EVENING,
            arabicText = "رَضِيتُ بِاللَّهِ رَبَّاً، وَبِالْإِسْلَامِ دِينَاً، وَبِمُحَمَّدٍ ﷺ نَبِيَّاً.",
            englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
            referenceAr = "رواه أحمد وأبو داود",
            referenceEn = "Ahmad & Abu Dawud",
            benefitAr = "من قالها ثلاثاً كان حقاً على الله أن يرضيه يوم القيامة.",
            benefitEn = "Recited three times, Allah promises to please the believer on the Day of Judgment.",
            initialCount = 3
        ),
        AdhkarItem(
            id = "e5",
            category = AdhkarCategory.EVENING,
            arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ.",
            englishTranslation = "O Allah, send peace and blessings upon our Prophet Muhammad.",
            referenceAr = "رواه الطبراني",
            referenceEn = "Narrated by At-Tabarani",
            benefitAr = "من صلى علي حين يصبح عشراً وحين يمسي عشراً أدركته شفاعتي يوم القيامة.",
            benefitEn = "Ten blessings morning and evening earn the Prophet's intercession.",
            initialCount = 10
        ),

        AdhkarItem(
            id = "p1",
            category = AdhkarCategory.POST_PRAYER,
            arabicText = "أَسْتَغْفِرُ اللَّهَ (ثلاثاً) اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ.",
            englishTranslation = "I seek Allah's forgiveness (3x). O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of Majesty and Honor.",
            referenceAr = "رواه مسلم",
            referenceEn = "Narrated by Muslim",
            benefitAr = "أول ما يبدأ به المسلم بعد السلام من الصلاة المكتوبة.",
            benefitEn = "The Prophetic opening immediately after concluding obligatory prayer.",
            initialCount = 1
        ),
        AdhkarItem(
            id = "p2",
            category = AdhkarCategory.POST_PRAYER,
            arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ.",
            englishTranslation = "None has the right to be worshipped except Allah alone, without partner. To Him belongs all sovereignty and praise, and He is over all things omnipotent. O Allah, none can withhold what You give, and none can give what You withhold, nor can wealth benefit anyone against You.",
            referenceAr = "متفق عليه",
            referenceEn = "Agreed Upon (Al-Bukhari & Muslim)",
            benefitAr = "إقرار بوحدانية الله وتفويض الأمر إليه بعد كل فرض.",
            benefitEn = "Affirming Allah's absolute sovereignty after every prayer.",
            initialCount = 1
        ),
        AdhkarItem(
            id = "p3",
            category = AdhkarCategory.POST_PRAYER,
            arabicText = "سُبْحَانَ اللَّهِ، وَالْحَمْدُ لِلَّهِ، وَاللَّهُ أَكْبَرُ.",
            englishTranslation = "Glory be to Allah, All praise be to Allah, and Allah is the Greatest.",
            referenceAr = "رواه مسلم",
            referenceEn = "Narrated by Muslim",
            benefitAr = "تُقال ثلاثاً وثلاثين مرة ثم يُختم المائة بالتوحيد، فتُغفر الخطايا.",
            benefitEn = "Recited 33 times each after prayer, completed to 100 with Tahlil.",
            initialCount = 33
        ),
        AdhkarItem(
            id = "p4",
            category = AdhkarCategory.POST_PRAYER,
            arabicText = "قراءة آية الكرسي: ﴿اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ...﴾",
            englishTranslation = "Reciting Ayat al-Kursi: Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence...",
            referenceAr = "رواه النسائي",
            referenceEn = "Narrated by An-Nasa'i",
            benefitAr = "من قرأها دبر كل صلاة مكتوبة لم يمنعه من دخول الجنة إلا الموت.",
            benefitEn = "Recited after every obligatory prayer, nothing stands between the believer and Paradise except death.",
            initialCount = 1
        ),
        AdhkarItem(
            id = "p5",
            category = AdhkarCategory.POST_PRAYER,
            arabicText = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ.",
            englishTranslation = "O Allah, help me to remember You, to give thanks to You, and to worship You in the best of manners.",
            referenceAr = "رواه أبو داود والنسائي",
            referenceEn = "Abu Dawud & An-Nasa'i",
            benefitAr = "وصية النبي ﷺ لمعاذ بن جبل دبر كل صلاة.",
            benefitEn = "The Prophet's ﷺ cherished advice to Mu'adh ibn Jabal after every prayer.",
            initialCount = 1
        )
    )
}
