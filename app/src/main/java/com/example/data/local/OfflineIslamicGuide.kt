package com.example.data.local

import com.example.data.model.AppLanguage

object OfflineIslamicGuide {

    fun answer(
        query: String,
        language: AppLanguage,
        cityName: String,
        hijriDate: String?,
        nextPrayerName: String?,
        nextPrayerTime: String?,
        remaining: String?
    ): String {
        val q = query.lowercase()
        val isEn = language == AppLanguage.ENGLISH
        val prefix = if (isEn) {
            "Offline Guide (No Internet Connection)"
        } else {
            "دليل ميقات المحلي (بدون إنترنت)"
        }

        val body = when {
            q.contains("سيد الاستغفار") || q.contains("istighfar") -> {
                if (isEn) {
                    """
                    **Sayyid al-Istighfar (Master Supplication for Forgiveness)**

                    Narrated by Shaddad ibn Aws in Sahih al-Bukhari:
                    "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant... I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for verily none can forgive sins except You."

                    **Meaning & Virtue:**
                    • It combines pure Tawhid (affirming Allah as Creator and Lord), humility, gratitude for blessings, and sincere repentance.
                    • The Prophet ﷺ said whoever recites it with firm conviction in the morning or evening and passes away that day/night enters Paradise.
                    """.trimIndent()
                } else {
                    """
                    **شرح سيد الاستغفار وفضله:**

                    «اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ... أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ» (رواه البخاري).

                    • **التوحيد والعبودية:** يبدأ بإقرار الربوبية والألوهية والاعتراف بالعبودية لله.
                    • **الاعتراف بالنعمة والتقصير:** «أبوء لك بنعمتك» أي أُقرّ وأعترف بنعمك، وأقرّ بذنبي طلباً للمغفرة.
                    • **الفضل العظيم:** من قاله موقناً به في الصباح أو المساء ومات في يومه أو ليلته دخل الجنة بإذن الله.
                    """.trimIndent()
                }
            }

            q.contains("خشوع") || q.contains("khushu") || q.contains("صلاة") || q.contains("salah") || q.contains("prayer") -> {
                if (isEn) {
                    """
                    **Practical Tips for Khushu (Focus & Serenity) in Salah:**

                    1. **Prepare Before the Takbir:** Perform Wudu calmly and respond to the Azan; clear urgent distractions before standing.
                    2. **Remember Standing Before Allah:** Recall that Allah responds to every verse of Surah Al-Fatihah you recite (Sahih Muslim).
                    3. **Ponder the Meanings:** Slow your recitation slightly and reflect on Al-Fatihah, Subhana Rabbiyal-Azim, and Subhana Rabbiyal-A'la.
                    4. **Look at the Place of Prostration:** Keep your gaze steady and treat every prayer as though it may be your farewell prayer.
                    """.trimIndent()
                } else {
                    """
                    **نصائح عملية للخشوع في الصلاة:**

                    1. **التهيئة قبل التكبير:** إسباغ الوضوء بهدوء، وترديد الأذان، وإفراغ القلب من المشاغل قبل الوقوف بين يدي الله.
                    2. **استحضار مناجاة الله:** تذكّر الحديث القدسي: «قسمت الصلاة بيني وبين عبدي نصفين» فالله يجيبك عند كل آية من الفاتحة.
                    3. **التأني والتدبر:** الترسل في القراءة، واستشعار معنى «سبحان ربي العظيم» في الركوع و«سبحان ربي الأعلى» في السجود.
                    4. **موضع السجود:** خفض البصر إلى موضع السجود، واستشعار أنها قد تكون صلاة مودّع.
                    """.trimIndent()
                }
            }

            q.contains("بعد الفريضة") || q.contains("بعد الصلاة") || q.contains("post") || q.contains("after") || q.contains("أذكار") || q.contains("adhkar") -> {
                if (isEn) {
                    """
                    **Blessed Adhkar After Obligatory Prayer:**

                    1. Say **Astaghfirullah** (3 times), then: *"Allahumma Antas-Salam wa minkas-Salam, tabarakta ya Dhal-Jalali wal-Ikram."*
                    2. Recite **Ayat al-Kursi** (Al-Baqarah 2:255) — nothing prevents entry to Paradise except death (An-Nasa'i).
                    3. **SubhanAllah (33), Alhamdulillah (33), Allahu Akbar (33)**, and complete 100 with *"La ilaha illallahu wahdahu la sharika lah..."*
                    4. Recite **Al-Ikhlas, Al-Falaq, and An-Nas** once after every prayer (three times after Fajr & Maghrib).
                    """.trimIndent()
                } else {
                    """
                    **أذكار مباركة ثابتة بعد الصلاة المفروضة:**

                    1. **أستغفر الله (3 مرات)**، ثم: «اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ» (رواه مسلم).
                    2. **آية الكرسي:** من قرأها دبر كل صلاة مكتوبة لم يمنعه من دخول الجنة إلا أن يموت (رواه النسائي).
                    3. **التسبيح:** سبحان الله (33)، الحمد لله (33)، الله أكبر (33)، وتمام المائة: «لا إله إلا الله وحده لا شريك له...».
                    4. **المعوذات:** الإخلاص والفلق والناس مرة بعد كل صلاة (وثلاثاً بعد الفجر والمغرب).
                    """.trimIndent()
                }
            }

            q.contains("دعاء") || q.contains("أدعية") || q.contains("dua") || q.contains("supplication") -> {
                if (isEn) {
                    """
                    **Authentic Prophetic Supplications (Duas):**

                    • *"Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar."* (Al-Baqarah 2:201)
                    • *"Allahumma a'inni 'ala dhikrika wa shukrika wa husni 'ibadatik."* (O Allah, help me remember You, thank You, and worship You well — Abu Dawud)
                    • *"Ya Muqallibal-qulub thabbit qalbi 'ala dinik."* (O Turner of hearts, keep my heart firm upon Your religion — At-Tirmidhi)
                    • *"Allahumma inni as'aluka 'ilman nafi'an, wa rizqan tayyiban, wa 'amalan mutaqabbalan."* (Ibn Majah)
                    """.trimIndent()
                } else {
                    """
                    **أدعية مأثورة جامعة من الكتاب والسنة:**

                    • ﴿رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ﴾.
                    • «اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ» (رواه أبو داود).
                    • «يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ» (رواه الترمذي).
                    • «اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا» (رواه ابن ماجه).
                    """.trimIndent()
                }
            }

            else -> {
                val contextLines = buildString {
                    appendLine(
                        if (isEn) "• Location: $cityName" else "• المدينة الحالية: $cityName"
                    )
                    if (!hijriDate.isNullOrBlank()) {
                        appendLine(
                            if (isEn) "• Hijri Date: $hijriDate" else "• التاريخ الهجري: $hijriDate"
                        )
                    }
                    if (!nextPrayerName.isNullOrBlank() && !nextPrayerTime.isNullOrBlank()) {
                        appendLine(
                            if (isEn) {
                                "• Next Prayer: $nextPrayerName at $nextPrayerTime (${remaining ?: ""})"
                            } else {
                                "• الصلاة القادمة: $nextPrayerName ($nextPrayerTime) — المتبقي ${remaining ?: ""}"
                            }
                        )
                    }
                }
                if (isEn) {
                    """
                    You are currently in **Offline Mode**, so cloud AI streaming is unavailable—however all core Meeqat features (astronomical prayer timings, Hijri calendar, Qibla compass, Tasbih, Adhkar reciter, and the Offline Quiz Bank) continue to work normally!

                    $contextLines
                    **Ask me locally about:**
                    • Sayyid al-Istighfar meaning
                    • Tips for Khushu in Salah
                    • Adhkar after obligatory prayers
                    • Authentic Prophetic Duas
                    """.trimIndent()
                } else {
                    """
                    أنت الآن في **وضع عدم الاتصال بالإنترنت**؛ لذا يتعذر الاتصال بالسحابة مؤقتاً، بينما تعمل جميع ميزات ميقات الأساسية محلياً بالكامل (حساب المواقيت الفلكي، التاريخ الهجري، القبلة، السبحة، الأذكار الصوتية، وبنك الأسئلة الدينية المحلي).

                    $contextLines
                    **يمكنني إجابتك محلياً فوراً حول:**
                    • معنى وفضل سيد الاستغفار
                    • نصائح عملية للخشوع في الصلاة
                    • الأذكار المأثورة بعد الفريضة
                    • أدعية نبوية مأثورة لوقت الصلاة
                    """.trimIndent()
                }
            }
        }

        return "[$prefix]\n\n$body"
    }
}
