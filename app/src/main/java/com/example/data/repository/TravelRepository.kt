package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first

class TravelRepository(private val travelDao: TravelDao) {

    val allOffers: Flow<List<Offer>> = travelDao.getAllOffersFlow()
    val allNews: Flow<List<NewsItem>> = travelDao.getAllNewsFlow()
    val allServices: Flow<List<ServiceItem>> = travelDao.getAllServicesFlow()
    val allSliderImages: Flow<List<SliderImage>> = travelDao.getAllSliderImagesFlow()
    val allInquiries: Flow<List<CustomerInquiry>> = travelDao.getAllInquiriesFlow()
    val allFAQs: Flow<List<FAQ>> = travelDao.getAllFAQsFlow()
    val appSettings: Flow<AppSettings?> = travelDao.getSettingsFlow()

    suspend fun checkAndPrepopulate() {
        // Run seed check
        val currentSettings = travelDao.getSettingsDirect()
        if (currentSettings == null) {
            seedDatabase()
        }
    }

    private suspend fun seedDatabase() {
        // 1. Seed Settings
        val defaultSettings = AppSettings(
            id = 1,
            officeAddress = "صنعاء - مجمع الشلال - المبنى الإداري - الدور السادس",
            phone1 = "737103025",
            phone2 = "738601113",
            landline = "01688617",
            email = "info@shanghaitravel.net",
            whatsapp = "+967737103025",
            snapchat = "shanghai.travel",
            facebook = "shanghaitravel.ye",
            instagram = "shanghaitravel",
            aboutUsText = "تأسست شركة شنغهاي للسياحة والسفر لتكون الشريك الموثوق والوجهة الأولى لكل مسافر يبحث عن التميز والراحة في السفر والترحال وحجز الرحلات السياحية والدينية والبرية بأسعار منافسة وجودة لا تضاهى.",
            visionText = "أن نكون الشركة الرائدة والأكثر ابتكاراً في قطاع السياحة والسفر والخدمات البرية والجوية على مستوى اليمن والمنطقة، واضعين راحة العميل وسلامته على رأس أولوياتنا.",
            missionText = "تقديم خدمات سفر وسياحة متكاملة وحلول حجوزات متطورة تفوق تطلعات عملائنا من خلال استخدام أحدث التقنيات وأسطول نقل متميز وفريق عمل متخصص يعمل على مدار الساعة.",
            valuesText = "الموثوقية والشفافية التامة في التعامل.\nالالتزام بأعلى معايير الأمان والجودة.\nالابتكار المستمر لتسهيل المعاملات.\nالتركيز التام على إرضاء العميل.",
            yearsOfExperience = 15
        )
        travelDao.insertSettings(defaultSettings)

        // 2. Seed Services
        val servicesList = listOf(
            ServiceItem(
                id = "flights",
                title = "حجوزات الطيران",
                description = "نوفر أفضل عروض الطيران على كافة الخطوط الجوية المحلية والدولية بأسعار تنافسية وسرعة فائقة.",
                features = "حجز فوري ومؤكد لجميع الوجهات العالمية والمحلية\nأسعار حصرية وتخفيضات موسمية تنافسية\nتعديل وإلغاء مرن للحجوزات بفضل علاقاتنا القوية\nدعم فني متواصل ومتابعة وتأكيد على مدار 24 ساعة",
                imageUrl = "flight_bg"
            ),
            ServiceItem(
                id = "hotels",
                title = "حجوزات الفنادق والإقامات",
                description = "حجوزات مضمونة في أرقى الفنادق والمنتجعات حول العالم وفي الأماكن المقدسة كـ مكة المكرمة والمدينة المنورة.",
                features = "فنادق ومجمعات سكنية قريبة ومميزة بأسعار منافسة جداً\nإمكانية اختيار نوع الإقامة المناسب (أجنحة، غرف فردية/زوجية)\nفريق متخصص لمتابعة إقامتك وخدمتك طوال الرحلة\nخيارات سداد متعددة ميسرة وآمنة",
                imageUrl = "hotel_bg"
            ),
            ServiceItem(
                id = "visas",
                title = "إصدار التأشيرات",
                description = "تسهيل استخراج ومعاملة جميع أنواع التأشيرات السياحية، الدينية، العلاجية والدراسية لشرق آسيا، دول الخليج، وأوروبا.",
                features = "تعبئة طلبات التأشيرات وتجهيز الملفات باحترافية كاملة\nمتابعة مستمرة ومباشرة مع السفارات والقنصليات المعنية\nنسب نجاح وقبول استثنائية وعالية جداً لكافة المعاملات\nاستشارات مجانية دقيقة لمتطلبات السفر لكل دولة",
                imageUrl = "visa_bg"
            ),
            ServiceItem(
                id = "hajj_umrah",
                title = "الحج والعمرة",
                description = "برامج متكاملة وميسرة لزيارة بيت الله الحرام ومسجد رسوله تشمل الإقامة الفاخرة، النقل المريح والإشراف الميداني المتكامل.",
                features = "فنادق VIP قريبة ومميزة من الحرم المكي والحرم المدني\nحافلات جديدة ومكيفة للتحركات بكل راحة وسلاسة\nمرشدون ومطوفون يمنيون ذوو خبرة لتوجيه المعتمرين\nخدمات الإعاشة وتوفير الرعاية الصحية والنفسية المتكاملة",
                imageUrl = "hajj_bg"
            ),
            ServiceItem(
                id = "yemen_transport",
                title = "النقل البري داخل اليمن",
                description = "رحلات برية آمنة ومنسقة بين كافة المحافظات والمدن اليمنية عبر أحدث أساطيل الحافلات المريحة المجهزة بالكامل.",
                features = "رحلات يومية منتظمة تربط صنعاء، عدن، تعز، حضرموت ومأرب\nحافلات VIP آمنة ومريحة ومكيفة لضمان رفاهيتك\nطاقم قيادة وسائقون محترفون ذوو خبرة عالية بجميع الطرقات\nأسعار ميسرة تناسب جميع الفئات والأسر والطلاب والمسافرين",
                imageUrl = "yemen_bus"
            ),
            ServiceItem(
                id = "intl_transport",
                title = "النقل البري خارج اليمن",
                description = "سفريات دولية برية متميزة تربط اليمن بدول الجوار كالمملكة العربية السعودية وسلطنة عمان بقمة الراحة والأمان والتنظيم.",
                features = "حافلات حديثة وفخمة مصممة للرحلات الطويلة الشاقة\nتنسيق وحل معاملات العبور في المنافذ والحدود الرسمية\nمواعيد دائمية ودقيقة لانطلاق والوصول وخطوط سير آمنة\nشواحن لهواتفك المحمولة، شاشات ترفيه ووجبات ضيافة مجاناً",
                imageUrl = "intl_bus"
            ),
            ServiceItem(
                id = "tourism_programs",
                title = "البرامج السياحية",
                description = "باقات وعروض سياحية متكاملة ومصممة خصيصاً في وجهات عالمية مذهلة كـ ماليزيا، تركيا، مصر، أذربيجان وشرق آسيا.",
                features = "برامج سياحية يومية منسقة بذكاء لاستثمار الوقت بشكل رائع\nتوفير مرشدين سياحيين محليين يتحدثون اللغة العربية بطلاقة\nتذاكر دخول المعالم، الفعاليات الترفيهية وجولات تسوق فريدة\nخيارات مرنة ومستويات فنادق ممتازة تناسب كافة الميزانيات",
                imageUrl = "tourism_bg"
            ),
            ServiceItem(
                id = "trips",
                title = "الرحلات الجماعية والفردية",
                description = "رحلات ترفيهية استكشافية جماعية للمؤسسات والأصدقاء، أو رحلات خاصة فردية وعائلية مصممة حسب رغبتكم بالكامل.",
                features = "تنسيق عالي الجودة للأنشطة والمسابقات التشاركية الجماعية\nخصوصية مطلقة لرحلات العائلات والأفراد المحبة للهدوء\nمساحات استكشاف فريدة وأماكن جديدة ومميزة\nألعاب وجداول مشوقة للكبار والصغار تضمن المتعة والفرح",
                imageUrl = "trips_bg"
            ),
            ServiceItem(
                id = "corporate",
                title = "خدمات الشركات والمؤسسات",
                description = "توفير كافة مستلزمات وحلول السفر المتكاملة لرحلات الأعمال، المؤتمرات، والوفود الخاصة بالشركات والهيئات.",
                features = "حسابات ائتمانية خاصة بالشركات مع تخفيضات مجزية دورية\nفواتير وتقارير مالية شهرية مفصلة لتسهيل الرقابة والمحاسبة\nترتيب كاف لرحلات رجال الأعمال والمؤتمرات بسرعة فائقة\nتخصيص مدير حسابات ذكي لمتابعة وإدارة كافة تفاصيل الحجز",
                imageUrl = "corporate_bg"
            )
        )
        for (srv in servicesList) {
            travelDao.insertService(srv)
        }

        // 3. Seed Offers
        val defaultOffers = listOf(
            Offer(
                title = "برنامج العمرة المتميزة لشهر شوال 1447",
                description = "برنامج عمرة متكامل بالباص يشمل السفر بحافلات VIP مريحة، الإقامة في فنادق 4 نجوم قريبة من الحرمين مع خط كامل للمزارات والإشراف الدائم.",
                imageUrl = "offer_shawal_umrah",
                startDate = "10-06-2026",
                endDate = "10-07-2026",
                isFeatured = true,
                isActive = true
            ),
            Offer(
                title = "تخفيض خاص 25% على رحلات تركيا الصيفية",
                description = "استمتع بصيف ساحر في اسطنبول والشمال التركي (طرابزون وأوزنجول) في باقة سياحية تشمل الفندق والرحلات الميدانية بأسعار حصرية للغاية لعملائنا.",
                imageUrl = "offer_turkey_summer",
                startDate = "15-06-2026",
                endDate = "31-08-2026",
                isFeatured = true,
                isActive = true
            ),
            Offer(
                title = "احجز رحلتك الدولية واحصل على كاش باك فوري",
                description = "خصم حصري على تذاكر الخطوط الجوية اليمنية للمغادرين إلى القاهرة، عمان، وجدة عند الحجز المباشر عبر تطبيق شركة شنغهاي للسياحة والسفر.",
                imageUrl = "offer_cashback_flights",
                startDate = "05-06-2026",
                endDate = "20-06-2026",
                isFeatured = false,
                isActive = true
            )
        )
        for (off in defaultOffers) {
            travelDao.insertOffer(off)
        }

        // 4. Seed News
        val defaultNews = listOf(
            NewsItem(
                title = "انطلاق أسطول حافلات VIP الجديد للرحلات الدولية",
                content = "في إطار التطوير المستمر لخدمات السفر والرحلات البرية الدولية، دشنت شركة شنغهاي أسطول حافلات VIP الجديد والأفخم من نوعه لخدمة الركاب المسافرين إلى المملكة العربية السعودية وسلطنة عمان. الحافلات مزودة بمقاعد مريحة للغاية، شاشات ترفيه فردية، خدمة إنترنت مجانية، ومنافذ شحن لكل مسافر لضمان سفر ممتع وآمن.",
                imageUrl = "news_bus_vip",
                date = "08-06-2026",
                isImportant = true
            ),
            NewsItem(
                title = "تكريم مكتب شنغهاي للرحلات كأفضل وكيل حصري وموثوق",
                content = "تم بفضل الله وتوفيقه وبثقتكم الغالية تتويج مكتب شنغهاي للسفريات والسياحة بدرع التميز والريادة كأفضل وكيل معتمد للخطوط الجوية ومجلس النقل البري في اليمن لهذا العام. نعدكم بمواصلة الالتزام بالجودة والشفافية وتسهيل جميع إجراءاتكم بأعلى درجة من الاحترافية والسرعة.",
                imageUrl = "news_award_prize",
                date = "01-06-2026",
                isImportant = false
            ),
            NewsItem(
                title = "تنبيه هام للمسافرين عبر منفذ الوديعة البري",
                content = "تود إدارة شركة شنغهاي للسياحة والسفر إفادة السادة المعتمرين والمسافرين الأعزاء بالالتزام بمطابقة كاف الفحوصات والوثائق الرسمية المطلوبة ومواعيد انطلاق الحافلات لتسهيل وتيسير عملية التفتيش والعبور بالمنفذ دون تأخير. للتواصل والاستعلام يرجى الاتصال بخدمة العملاء المباشرة.",
                imageUrl = "news_alert_passengers",
                date = "29-05-2026",
                isImportant = true
            )
        )
        for (ns in defaultNews) {
            travelDao.insertNews(ns)
        }

        // 5. Seed Slider Images
        val defaultSliders = listOf(
            SliderImage(imageUrl = "slide_flight", caption = "حجوزات طيران دولية ومحلية بأفضل الأسعار المتاحة"),
            SliderImage(imageUrl = "slide_hajj", caption = "حملات حج وعمرة منتقاة بأعلى مستويات الإقامة والإشراف الديني الكفوء"),
            SliderImage(imageUrl = "slide_bus", caption = "رحلات نقل بري يومية مريحة وآمنة داخل وخارج اليمن"),
            SliderImage(imageUrl = "slide_hotel", caption = "إقامات فندقية فاخرة في مكة والمدينة وحول العالم")
        )
        for (sl in defaultSliders) {
            travelDao.insertSliderImage(sl)
        }

        // 6. Seed FAQs
        val defaultFAQs = listOf(
            FAQ(
                question = "ما هي المستندات المطلوبة لاستخراج تأشيرة العمرة ومعاملتها لدى مكتبكم؟",
                answer = "المستندات الأساسية هي جواز سفر أصلي صالح لمدة 6 أشهر على الأقل، صورة شخصية حديثة بخلفية بيضاء، فحص طبي معتمد للأمراض المعدية، ودفتر التحصينات المعتمد، والتسجيل عبر مكتبنا لإتمام الحجز الفندقي والتنسيقات."
            ),
            FAQ(
                question = "هل يمكنني تعديل حجز السفر أو إلغاء التذكرة واسترجاع قيمتها؟",
                answer = "نعم بكل تأكيد، التعديل أو الإلغاء يعتمد على السياسة المحددة لكل شركة طيران أو مالكي وسيلة النقل البري. نحن نبذل قصارى جهدنا للتنسيق السريع وتقديم خيارات تعديل مرنة ومطالبة الاسترداد النقدي بأقل رسوم إدارية ممكنة."
            ),
            FAQ(
                question = "ما هي التغطية الجغرافية لخدمات النقل البري المتاحة في شركة شنغهاي؟",
                answer = "نوفر تغطية شاملة لجميع المحافظات وحواضر المدن اليمنية الرئيسية (صنعاء، عدن، تعز، المكلا، مأرب، الحديدة، سيئون)، كما نسير خطوط نقل بري دولية فاخرة منتظمة ومباشرة إلى مناطق مختلفة بالسعودية وسلطنة عمان."
            ),
            FAQ(
                question = "هل تقدمون عروض أسعار وخدمات خاصة للمجموعات الكبيرة والجهات الحكومية والشركات؟",
                answer = "نعم، لدينا قسم كامل متخصص يدعى 'خدمات الشركات والمؤسسات' يقدم حلول سداد ميسرة، أسعار تنافسية مخفضة بشكل دوري للمجموعات والوفود الرسمية، وتخصيص مدير اتصال للتنسيق وتجهيز كاف الإجراءات بكفاءة تامة."
            )
        )
        for (fq in defaultFAQs) {
            travelDao.insertFAQ(fq)
        }
    }

    // Insert Inquiry
    suspend fun submitInquiry(name: String, phone: String, service: String, details: String) {
        val inquiry = CustomerInquiry(
            clientName = name,
            clientPhone = phone,
            serviceType = service,
            details = details
        )
        travelDao.insertInquiry(inquiry)
    }

    // Offers actions
    suspend fun addOffer(offer: Offer) = travelDao.insertOffer(offer)
    suspend fun updateOffer(offer: Offer) = travelDao.updateOffer(offer)
    suspend fun deleteOfferById(id: Int) = travelDao.deleteOfferById(id)

    // News actions
    suspend fun addNews(newsItem: NewsItem) = travelDao.insertNews(newsItem)
    suspend fun updateNews(newsItem: NewsItem) = travelDao.updateNews(newsItem)
    suspend fun deleteNewsById(id: Int) = travelDao.deleteNewsById(id)

    // Services actions
    suspend fun updateService(serviceItem: ServiceItem) = travelDao.updateService(serviceItem)

    // Slider actions
    suspend fun addSliderImage(sliderImage: SliderImage) = travelDao.insertSliderImage(sliderImage)
    suspend fun deleteSliderImageById(id: Int) = travelDao.deleteSliderImageById(id)

    // FAQS actions
    suspend fun addFAQ(faq: FAQ) = travelDao.insertFAQ(faq)
    suspend fun updateFAQ(faq: FAQ) = travelDao.updateFAQ(faq)
    suspend fun deleteFAQById(id: Int) = travelDao.deleteFAQById(id)

    // Inquiries actions
    suspend fun updateInquiryStatus(id: Int, status: String) = travelDao.updateInquiryStatus(id, status)
    suspend fun deleteInquiry(inquiry: CustomerInquiry) = travelDao.deleteInquiry(inquiry)

    // Settings actions
    suspend fun updateSettings(settings: AppSettings) = travelDao.insertSettings(settings)
}
