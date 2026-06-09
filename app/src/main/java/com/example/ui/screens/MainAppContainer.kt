package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.*
import com.example.ui.theme.ShanghaiBlue
import com.example.ui.theme.ShanghaiGold
import com.example.ui.theme.ShanghaiPink
import com.example.viewmodel.AppNotification
import com.example.viewmodel.TravelViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Master Container to route and manage screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: TravelViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect App State
    val offers by viewModel.allOffers.collectAsStateWithLifecycle()
    val news by viewModel.allNews.collectAsStateWithLifecycle()
    val services by viewModel.allServices.collectAsStateWithLifecycle()
    val sliderImages by viewModel.allSliderImages.collectAsStateWithLifecycle()
    val inquiries by viewModel.allInquiries.collectAsStateWithLifecycle()
    val faqs by viewModel.allFAQs.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val headsUpNotif by viewModel.headsUpNotification.collectAsStateWithLifecycle()

    // UI Configuration States
    var currentTab by remember { mutableStateOf("home") } // home, services, offers, contact, about
    var selectedServiceId by remember { mutableStateOf<String?>(null) }
    var selectedNewsItem by remember { mutableStateOf<NewsItem?>(null) }
    var showInquiryDialogForService by remember { mutableStateOf<String?>(null) }
    var showNotificationCenter by remember { mutableStateOf(false) }

    // Admin panel access state
    var showAdminPanel by remember { mutableStateOf(false) }
    var isAdminAuthenticated by remember { mutableStateOf(false) }
    var adminPasswordInput by remember { mutableStateOf("") }
    var adminPasswordError by remember { mutableStateOf(false) }

    // RTL Enforcement
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Logo Icon: Suitcase shape with airplane
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(ShanghaiBlue, Color(0xFF0B2D75))
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlightTakeoff,
                                    contentDescription = null,
                                    tint = ShanghaiGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "شنغهاي للسياحة",
                                fontWeight = FontWeight.Bold,
                                color = ShanghaiBlue,
                                fontSize = 18.sp
                            )
                        }
                    },
                    navigationIcon = {
                        // Admin panel trigger
                        IconButton(
                            onClick = {
                                if (isAdminAuthenticated) {
                                    showAdminPanel = !showAdminPanel
                                } else {
                                    showAdminPanel = true
                                }
                            },
                            modifier = Modifier.testTag("admin_panel_trigger")
                        ) {
                            Icon(
                                imageVector = if (showAdminPanel) Icons.Default.ArrowBack else Icons.Default.AdminPanelSettings,
                                contentDescription = "لوحة التحكم",
                                tint = if (showAdminPanel) ShanghaiPink else ShanghaiBlue
                            )
                        }
                    },
                    actions = {
                        // Alerts bell
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(onClick = { showNotificationCenter = !showNotificationCenter }) {
                                Icon(
                                    imageVector = if (notifications.any { !it.isRead }) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                                    contentDescription = "التنبيهات",
                                    tint = if (notifications.any { !it.isRead }) ShanghaiPink else ShanghaiBlue
                                )
                            }
                            if (notifications.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .size(8.dp)
                                        .background(ShanghaiPink, shape = CircleShape)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = ShanghaiBlue
                    )
                )
            },
            bottomBar = {
                if (!showAdminPanel) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        val items = listOf(
                            Triple("home", "الرئيسية", Icons.Default.Home),
                            Triple("services", "خدماتنا", Icons.Default.GridView),
                            Triple("offers", "عروضنا", Icons.Default.LocalOffer),
                            Triple("about", "من نحن", Icons.Default.Info),
                            Triple("contact", "اتصل بنا", Icons.Default.Phone)
                        )
                        items.forEach { (route, label, icon) ->
                            val selected = currentTab == route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    currentTab = route
                                    selectedServiceId = null
                                    selectedNewsItem = null
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (selected) ShanghaiBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = ShanghaiBlue.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.testTag("nav_$route")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Main Content Switching
                AnimatedContent(
                    targetState = showAdminPanel,
                    transitionSpec = {
                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                    },
                    label = "AdminTransition"
                ) { isCurrentlyAdmin ->
                    if (isCurrentlyAdmin) {
                        // Admin Panel Layer
                        if (!isAdminAuthenticated) {
                            AdminAuthScreen(
                                passwordInput = adminPasswordInput,
                                onPasswordChange = { adminPasswordInput = it },
                                isError = adminPasswordError,
                                onAuthenticate = {
                                    if (adminPasswordInput == "shanghai2026" || adminPasswordInput == "123456") {
                                        isAdminAuthenticated = true
                                        adminPasswordInput = ""
                                        adminPasswordError = false
                                    } else {
                                        adminPasswordError = true
                                    }
                                },
                                onCancel = {
                                    showAdminPanel = false
                                    adminPasswordInput = ""
                                    adminPasswordError = false
                                }
                            )
                        } else {
                            AdminPanelDashboard(
                                viewModel = viewModel,
                                offers = offers,
                                news = news,
                                services = services,
                                sliderImages = sliderImages,
                                inquiries = inquiries,
                                faqs = faqs,
                                settings = settings,
                                onExit = {
                                    showAdminPanel = false
                                    isAdminAuthenticated = false
                                }
                            )
                        }
                    } else {
                        // Customer App Layer
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (currentTab) {
                                "home" -> HomeScreen(
                                    viewModel = viewModel,
                                    offers = offers,
                                    news = news,
                                    services = services,
                                    sliderImages = sliderImages,
                                    settings = settings,
                                    onServiceSelected = { srvId ->
                                        selectedServiceId = srvId
                                        currentTab = "services"
                                    },
                                    onOfferSelected = {
                                        currentTab = "offers"
                                    },
                                    onNewsSelected = { nItem ->
                                        selectedNewsItem = nItem
                                    }
                                )
                                "services" -> ServicesTab(
                                    services = services,
                                    selectedServiceId = selectedServiceId,
                                    onBackToServicesList = { selectedServiceId = null },
                                    onServiceClicked = { serviceId ->
                                        selectedServiceId = serviceId
                                    },
                                    onInquiryRequired = { srvId ->
                                        showInquiryDialogForService = srvId
                                    },
                                    settings = settings
                                )
                                "offers" -> OffersTab(
                                    offers = offers,
                                    onApplyOffer = { offerTitle ->
                                        showInquiryDialogForService = "عرض: $offerTitle"
                                    }
                                )
                                "about" -> AboutUsTab(
                                    settings = settings,
                                    faqs = faqs
                                )
                                "contact" -> ContactTab(
                                    settings = settings,
                                    onSubmitGeneralForm = { name, phone, message ->
                                        viewModel.submitInquiry(
                                            name = name,
                                            phone = phone,
                                            service = "استفسار عام",
                                            details = message,
                                            onSuccess = {
                                                Toast.makeText(context, "تم إرسال استفسارك بنجاح وسنتواصل معك قريباً", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                )
                            }

                            // Detail overlay for news item clicked from home
                            selectedNewsItem?.let { selectedNews ->
                                NewsItemDetailDialog(
                                    newsItem = selectedNews,
                                    onDismiss = { selectedNewsItem = null }
                                )
                            }
                        }
                    }
                }

                // Unified Customer Inquiry Input Dialog
                showInquiryDialogForService?.let { requestedService ->
                    InquiryDialog(
                        serviceTitle = requestedService,
                        onDismiss = { showInquiryDialogForService = null },
                        onSubmit = { name, phone, details ->
                            viewModel.submitInquiry(
                                name = name,
                                phone = phone,
                                service = requestedService,
                                details = details,
                                onSuccess = {
                                    Toast.makeText(context, "تم إرسال طلب الحجز وسنقوم بالتواصل بك هاتفياً وتأكيد طلبك قريباً", Toast.LENGTH_LONG).show()
                                    showInquiryDialogForService = null
                                }
                            )
                        }
                    )
                }

                // Notification Center Popup Overlay
                if (showNotificationCenter) {
                    NotificationCenterDialog(
                        notifications = notifications,
                        onDismiss = { showNotificationCenter = false },
                        onClearAll = { viewModel.clearAllNotifications() }
                    )
                }

                // Heads-Up Floating Custom Push banner (Simulating notification arriving)
                headsUpNotif?.let { notif ->
                    HeadsUpNotificationBanner(
                        notification = notif,
                        onDismiss = { viewModel.dismissHeadsUp() }
                    )
                }
            }
        }
    }
}

// ==========================================
// HOME TAB
// ==========================================
@Composable
fun HomeScreen(
    viewModel: TravelViewModel,
    offers: List<Offer>,
    news: List<NewsItem>,
    services: List<ServiceItem>,
    sliderImages: List<SliderImage>,
    settings: AppSettings?,
    onServiceSelected: (String) -> Unit,
    onOfferSelected: (Offer) -> Unit,
    onNewsSelected: (NewsItem) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Sleek Welcome Header Card with Logo Background Canvas
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(ShanghaiGold.copy(0.3f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ShanghaiBlue, Color(0xFF0F265C))
                        )
                    )
                    .drawBehind {
                        // Drawing airplane contrail
                        val p = Path()
                        p.moveTo(0f, size.height * 0.8f)
                        p.quadraticTo(
                            size.width * 0.4f,
                            size.height * 0.6f,
                            size.width * 0.9f,
                            size.height * 0.2f
                        )
                        drawPath(
                            path = p,
                            color = Color.White.copy(alpha = 0.15f),
                            style = Stroke(width = 8f)
                        )
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Custom Draw suitcases representation
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(ShanghaiPink, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TravelExplore,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "مرحباً بكم في شنغهاي للسياحة",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "بوابتكم الموثوقة لحجوزات الطيران والنقل البري المتكامل",
                            color = Color.White.copy(0.85f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 2. Sliding Header Banner representing flights, hajj, hotel
        item {
            SliderCarousel(sliderImages = sliderImages)
        }

        // 3. Quick Action Buttons: Whatsapp and Call
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "التواصل السريع المباشر للحجز والإستعلام المباشر",
                        fontWeight = FontWeight.Bold,
                        color = ShanghaiBlue,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                openWhatsapp(context, settings?.whatsapp ?: "+967737103025")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_quick_whatsapp")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("واتساب مباشر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                makeCall(context, settings?.phone1 ?: "737103025")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShanghaiPink),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_quick_call")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اتصال هاتفي", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 4. Services Grid section (cards with custom iconic themes)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "خدماتنا المتميزة الفاخرة",
                    fontWeight = FontWeight.Bold,
                    color = ShanghaiBlue,
                    fontSize = 16.sp
                )
                // Filter only visible services
                val visibleServices = services.filter { it.isVisible }
                RowGrid(items = visibleServices, columns = 3) { srv ->
                    ServiceCard(service = srv, onClick = { onServiceSelected(srv.id) })
                }
            }
        }

        // 5. Featured Offers Carousel / Grid and Expiry
        item {
            val featuredOffers = offers.filter { it.isFeatured && it.isActive }
            if (featuredOffers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "أحدث العروض الحصرية والخصومات",
                            fontWeight = FontWeight.Bold,
                            color = ShanghaiBlue,
                            fontSize = 16.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(ShanghaiPink.copy(0.15f), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "مميز",
                                color = ShanghaiPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    featuredOffers.forEach { offer ->
                        OfferHomeCard(offer = offer, onClick = { onOfferSelected(offer) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 6. News & Announcements feed
        item {
            if (news.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "آخر الأخبار والتنبيهات المباشرة",
                        fontWeight = FontWeight.Bold,
                        color = ShanghaiBlue,
                        fontSize = 16.sp
                    )
                    news.take(3).forEach { newsItem ->
                        NewsItemRowCard(newsItem = newsItem, onClick = { onNewsSelected(newsItem) })
                    }
                }
            }
        }

        // 7. Client Reviews / Testimonials Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "آراء عملائنا الكرام وثقتهم",
                    fontWeight = FontWeight.Bold,
                    color = ShanghaiBlue,
                    fontSize = 16.sp
                )
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    val reviews = listOf(
                        Pair("أبو رائد اليافعي", "أفضل مكتب تعاملت معه في حجوزات الطيران والنقل البري، مصداقية تامة في المواعيد وسرعة في الرد."),
                        Pair("م. عادل الشميري", "خدمة إصدار التأشيرات لديهم ممتازة، قاموا بتسهيل المعاملة في غضون سويعات بسيطة وتواصل مستمر."),
                        Pair("الحاج محمد القاضي", "رحلتنا في العمرة لشهر رجب كانت ميسرة للغاية بفضل الله والمشرف المتكامل من شركة شنغهاي.")
                    )
                    reviews.forEach { (reviewer, text) ->
                        Card(
                            modifier = Modifier
                                .width(250.dp)
                                .padding(end = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ShanghaiGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ShanghaiGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ShanghaiGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ShanghaiGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ShanghaiGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = text,
                                    fontSize = 11.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = reviewer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ShanghaiBlue,
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom Grid Layout helper for LazyColumn
@Composable
fun <T> RowGrid(
    items: List<T>,
    columns: Int,
    itemContent: @Composable (T) -> Unit
) {
    val rows = (items.size + columns - 1) / columns
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (c in 0 until columns) {
                    val index = r * columns + c
                    Box(modifier = Modifier.weight(1f)) {
                        if (index < items.size) {
                            itemContent(items[index])
                        } else {
                            Spacer(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

// Dynamic Slider Carousel representing Flight, Hajj / Umrah, Hotel imagery
@Composable
fun SliderCarousel(sliderImages: List<SliderImage>) {
    var activeIndex by remember { mutableStateOf(0) }

    // Auto-advance
    LaunchedEffect(sliderImages.size) {
        if (sliderImages.isNotEmpty()) {
            while (true) {
                delay(4000)
                activeIndex = (activeIndex + 1) % sliderImages.size
            }
        }
    }

    if (sliderImages.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background brush/gradient representing travel and aviation
                val activeSlider = sliderImages[activeIndex]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ShanghaiBlue.copy(0.9f),
                                    ShanghaiPink.copy(0.8f)
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom Abstract Vector drawing matching background
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = when (activeIndex) {
                                0 -> Icons.Default.FlightTakeoff
                                1 -> Icons.Default.Mosque
                                2 -> Icons.Default.DirectionsBus
                                else -> Icons.Default.Hotel
                            },
                            contentDescription = null,
                            tint = ShanghaiGold,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeSlider.caption,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Overlay dot indicators
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sliderImages.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == activeIndex) 16.dp else 8.dp, 8.dp)
                                .background(
                                    color = if (idx == activeIndex) ShanghaiGold else Color.White.copy(0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

// Service Grid Card implementation
@Composable
fun ServiceCard(service: ServiceItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.95f)
            .clickable(onClick = onClick)
            .testTag("service_card_${service.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Service specific icon mapping
            val serviceIcon = when (service.id) {
                "flights" -> Icons.Default.Flight
                "hotels" -> Icons.Default.Hotel
                "visas" -> Icons.Default.Assignment
                "hajj_umrah" -> Icons.Default.Mosque
                "yemen_transport" -> Icons.Default.DirectionsBus
                "intl_transport" -> Icons.Default.TravelExplore
                "tourism_programs" -> Icons.Default.Map
                "trips" -> Icons.Default.Groups
                "corporate" -> Icons.Default.Business
                else -> Icons.Default.Luggage
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(ShanghaiBlue.copy(alpha = 0.08f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = serviceIcon,
                    contentDescription = service.title,
                    tint = ShanghaiBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = service.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ShanghaiBlue,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Offer Card in home page list
@Composable
fun OfferHomeCard(offer: Offer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("offer_home_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large dynamic ticket coupon accent
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(ShanghaiPink, ShanghaiBlue)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Percent,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = offer.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ShanghaiBlue
                )
                Text(
                    text = offer.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "يبدأ: ${offer.startDate}",
                        fontSize = 10.sp,
                        color = ShanghaiPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ينتهي: ${offer.endDate}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Premium news feed row list item in home
@Composable
fun NewsItemRowCard(newsItem: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("news_item_row"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ShanghaiBlue.copy(alpha = 0.08f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (newsItem.isImportant) Icons.Default.NotificationImportant else Icons.Default.Feed,
                        contentDescription = null,
                        tint = if (newsItem.isImportant) ShanghaiPink else ShanghaiBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = newsItem.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ShanghaiBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = newsItem.content,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = newsItem.date,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// Dialog details modal for dynamic news row clicks
@Composable
fun NewsItemDetailDialog(newsItem: NewsItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = newsItem.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ShanghaiBlue,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Graphic
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ShanghaiBlue, ShanghaiPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "التاريخ: ${newsItem.date}",
                        fontSize = 11.sp,
                        color = ShanghaiPink,
                        fontWeight = FontWeight.Bold
                    )
                    if (newsItem.isImportant) {
                        Text(
                            text = "إعلان هام وعاجل",
                            fontSize = 11.sp,
                            color = ShanghaiPink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = newsItem.content,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue)
            ) {
                Text("إغلاق", color = Color.White)
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp)
    )
}

// ==========================================
// SERVICES TAB
// ==========================================
@Composable
fun ServicesTab(
    services: List<ServiceItem>,
    selectedServiceId: String?,
    onBackToServicesList: () -> Unit,
    onServiceClicked: (String) -> Unit,
    onInquiryRequired: (String) -> Unit,
    settings: AppSettings?
) {
    val context = LocalContext.current

    AnimatedContent(
        targetState = selectedServiceId,
        label = "ServicesTabContentTransition"
    ) { activeSrvId ->
        if (activeSrvId == null) {
            // 1. Service Cards List View
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "الخدمات السياحية واللوجستية الشاملة",
                        fontWeight = FontWeight.Bold,
                        color = ShanghaiBlue,
                        fontSize = 16.sp
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("services_grid"),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(services.filter { it.isVisible }) { srvItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clickable { onServiceClicked(srvItem.id) }
                                .testTag("srv_grid_card_${srvItem.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(ShanghaiBlue.copy(0.08f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getServiceIconById(srvItem.id),
                                        contentDescription = srvItem.title,
                                        tint = ShanghaiPink,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = srvItem.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ShanghaiBlue,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // 2. Specific Service Detail Page
            val activeSrv = services.find { it.id == activeSrvId }
            activeSrv?.let { srv ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("service_detail_column"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = onBackToServicesList) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "الرجوع للخدمات",
                                    tint = ShanghaiBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "تفاصيل الخدمة: ${srv.title}",
                                fontWeight = FontWeight.Bold,
                                color = ShanghaiBlue,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Top Banner Graphic Draw
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(ShanghaiBlue, ShanghaiPink)
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = getServiceIconById(srv.id),
                                    contentDescription = null,
                                    tint = ShanghaiGold,
                                    modifier = Modifier.size(45.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    srv.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    // Description text
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "وصف تفصيلي للخدمة",
                                    fontWeight = FontWeight.Bold,
                                    color = ShanghaiBlue,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = srv.description,
                                    fontSize = 13.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Justify
                                )
                            }
                        }
                    }

                    // Features block
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "مميزات ومزايا الخدمة",
                                    fontWeight = FontWeight.Bold,
                                    color = ShanghaiBlue,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                // Parse lines
                                val featureLines = srv.features.split("\n").filter { it.isNotBlank() }
                                featureLines.forEach { line ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = ShanghaiPink,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .offset(y = 2.dp)
                                        )
                                        Text(
                                            text = line,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action tools: Request Inquiry + Call + Whatsapp
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "اطلب الخدمة الآن مباشرة",
                                    fontWeight = FontWeight.Bold,
                                    color = ShanghaiBlue,
                                    fontSize = 14.sp
                                )

                                Button(
                                    onClick = { onInquiryRequired(srv.title) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_for_${srv.id}"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddBox,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تعبئة طلب حجز واستفسار تليفوني", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            openWhatsapp(context, settings?.whatsapp ?: "+967737103025")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("واتساب مباشر", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            makeCall(context, settings?.phone1 ?: "737103025")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ShanghaiPink),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("اتصال هاتفي", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Icon Mapping Function
fun getServiceIconById(id: String): ImageVector {
    return when (id) {
        "flights" -> Icons.Default.Flight
        "hotels" -> Icons.Default.Hotel
        "visas" -> Icons.Default.Assignment
        "hajj_umrah" -> Icons.Default.Mosque
        "yemen_transport" -> Icons.Default.DirectionsBus
        "intl_transport" -> Icons.Default.TravelExplore
        "tourism_programs" -> Icons.Default.Map
        "trips" -> Icons.Default.Groups
        "corporate" -> Icons.Default.Business
        else -> Icons.Default.Luggage
    }
}

// ==========================================
// OFFERS TAB
// ==========================================
@Composable
fun OffersTab(
    offers: List<Offer>,
    onApplyOffer: (String) -> Unit
) {
    var filterFeaturedOnly by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("offers_tab_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "العروض والمناقصات السياحية المتاحة",
                    fontWeight = FontWeight.Bold,
                    color = ShanghaiBlue,
                    fontSize = 16.sp
                )
                Text(
                    text = "اكتشف باقات الخصم والعروض الترويجية لفترات محدودة بأسعار تخدم متطلباتكم.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Optional Filter Buttons
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = !filterFeaturedOnly,
                        onClick = { filterFeaturedOnly = false },
                        label = { Text("جميع العروض والمناقصات") }
                    )
                    FilterChip(
                        selected = filterFeaturedOnly,
                        onClick = { filterFeaturedOnly = true },
                        label = { Text("العروض الهامة / مميز") }
                    )
                }
            }
        }

        val filteredOffers = if (filterFeaturedOnly) {
            offers.filter { it.isFeatured && it.isActive }
        } else {
            offers.filter { it.isActive }
        }

        if (filteredOffers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SentimentDissatisfied,
                            contentDescription = null,
                            tint = ShanghaiPink.copy(0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "لا توجد عروض ترويجية نشطة حالياً",
                            fontWeight = FontWeight.Bold,
                            color = ShanghaiBlue,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredOffers) { offer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Top Graphic header representing the offer destination
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(ShanghaiBlue, ShanghaiPink)
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    if (offer.isFeatured) {
                                        Box(
                                            modifier = Modifier
                                                .background(ShanghaiGold, shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "عرض هام ومحدود",
                                                color = ShanghaiBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    Text(
                                        offer.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = ShanghaiGold,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        // Offer Details
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                offer.description,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "تاريخ البدء: ${offer.startDate}",
                                        fontSize = 11.sp,
                                        color = ShanghaiPink,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "تاريخ الانتهاء: ${offer.endDate}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { onApplyOffer(offer.title) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("get_offer_btn")
                                ) {
                                    Text("احصل على العرض", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ABOUT US TAB & FAQS
// ==========================================
@Composable
fun AboutUsTab(settings: AppSettings?, faqs: List<FAQ>) {
    var expandedFaqId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("about_tab_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Experience Badge & Intro
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(ShanghaiBlue, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${settings?.yearsOfExperience ?: 15}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShanghaiGold
                            )
                            Text(
                                text = "عاماً خبرة",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "من نحن",
                            fontWeight = FontWeight.Bold,
                            color = ShanghaiBlue,
                            fontSize = 18.sp
                        )
                        Text(
                            text = settings?.aboutUsText ?: "شركة شنغهاي للسفريات والسياحة شريككم المعتمد عبر السبل والطرق المريحة.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Vision, Mission & Values
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "رؤيتنا ورسالتنا وقيمنا",
                    fontWeight = FontWeight.Bold,
                    color = ShanghaiBlue,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Vision Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = ShanghaiPink)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("رؤية الشركة", fontWeight = FontWeight.Bold, color = ShanghaiBlue, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = settings?.visionText ?: "",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Mission Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, tint = ShanghaiPink)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("رسالتنا", fontWeight = FontWeight.Bold, color = ShanghaiBlue, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = settings?.missionText ?: "",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Values Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.StarBorder, contentDescription = null, tint = ShanghaiPink)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("القيم والمبادئ الحاكمة", fontWeight = FontWeight.Bold, color = ShanghaiBlue, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val valText = settings?.valuesText ?: ""
                        valText.split("\n").filter { it.isNotBlank() }.forEach { line ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .offset(y = 6.dp)
                                        .background(ShanghaiGold, shape = CircleShape)
                                )
                                Text(line, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // FAQs Accordion
        item {
            Text(
                text = "الأسئلة الشائعة والأجوبة",
                fontWeight = FontWeight.Bold,
                color = ShanghaiBlue,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (faqs.isEmpty()) {
            item {
                Text(
                    "لا توجد أسئلة شائعة مضافة حالياً",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(faqs) { faq ->
                val isExpanded = expandedFaqId == faq.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedFaqId = if (isExpanded) null else faq.id }
                        .testTag("faq_item_${faq.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = faq.question,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ShanghaiBlue,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = ShanghaiPink
                            )
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = faq.answer,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// CONTACT TAB
// ==========================================
@Composable
fun ContactTab(
    settings: AppSettings?,
    onSubmitGeneralForm: (String, String, String) -> Unit
) {
    var senderName by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf("") }
    var inquiryMessage by remember { mutableStateOf("") }
    var isFormValid by remember { mutableStateOf(false) }

    LaunchedEffect(senderName, senderPhone, inquiryMessage) {
        isFormValid = senderName.isNotBlank() && senderPhone.isNotBlank() && inquiryMessage.isNotBlank()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("contact_tab_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Direct contact cards (address, phonenumber, email)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "معلومات وتفاصيل الاتصال بنا والموقع الرسمي",
                        fontWeight = FontWeight.Bold,
                        color = ShanghaiBlue,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Address
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = ShanghaiPink)
                        Column {
                            Text("العنوان والمقر الرئيسي:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShanghaiBlue)
                            Text(settings?.officeAddress ?: "صنعاء - مجمع الشلال - المبنى الإداري - الدور السادس", fontSize = 12.sp)
                        }
                    }

                    // Phones
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = ShanghaiPink)
                        Column {
                            Text("أرقام الهواتف والتواصل السريع:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShanghaiBlue)
                            Text("هاتف جوال الرئيسي: ${settings?.phone1 ?: "737103025"}", fontSize = 12.sp)
                            Text("هاتف جوال المتابعة: ${settings?.phone2 ?: "738601113"}", fontSize = 12.sp)
                            Text("تلفون خط أرضي: ${settings?.landline ?: "01688617"}", fontSize = 12.sp)
                        }
                    }

                    // Email
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = ShanghaiPink)
                        Column {
                            Text("البريد الإلكتروني المباشر:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShanghaiBlue)
                            Text(settings?.email ?: "info@shanghaitravel.net", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Beautiful Live Map Placeholders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "الموقع الجغرافي على الخريطة",
                        fontWeight = FontWeight.Bold,
                        color = ShanghaiBlue,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Custom Draw beautiful maps
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E5E9))
                            .drawBehind {
                                // Mock drawing of map grids
                                val gridColor = Color(0xFFC0CCD4)
                                for (i in 0..size.width.toInt() step 60) {
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(i.toFloat(), 0f),
                                        end = Offset(i.toFloat(), size.height),
                                        strokeWidth = 2f
                                    )
                                }
                                for (i in 0..size.height.toInt() step 60) {
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, i.toFloat()),
                                        end = Offset(size.width, i.toFloat()),
                                        strokeWidth = 2f
                                    )
                                }
                                // Main office pin
                                drawCircle(
                                    color = ShanghaiBlue,
                                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                                    radius = 16f
                                )
                                drawCircle(
                                    color = ShanghaiPink,
                                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                                    radius = 8f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(0.9f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                "صنعاء - مجمع الشلال - مبنى الإدارة",
                                color = ShanghaiBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Contact feedback Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "أرسل لنا استفسارك مباشرة (تواصل فوري)",
                        fontWeight = FontWeight.Bold,
                        color = ShanghaiBlue,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = { Text("الاسم الكريم") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_form_name"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShanghaiBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    OutlinedTextField(
                        value = senderPhone,
                        onValueChange = { senderPhone = it },
                        label = { Text("رقم هاتفك للتواصل") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_form_phone"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShanghaiBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    OutlinedTextField(
                        value = inquiryMessage,
                        onValueChange = { inquiryMessage = it },
                        label = { Text("تفاصيل الرسالة أو الاستفسار") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("contact_form_msg"),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShanghaiBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Button(
                        onClick = {
                            onSubmitGeneralForm(senderName, senderPhone, inquiryMessage)
                            senderName = ""
                            senderPhone = ""
                            inquiryMessage = ""
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShanghaiBlue,
                            disabledContainerColor = ShanghaiBlue.copy(0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("contact_form_submit"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إرسال الآن هاتفياً", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Phone Call Trigger Method
fun makeCall(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "عملية الاتصال المباشر غير مدعومة على هذا الجهاز بقيمة $number", Toast.LENGTH_SHORT).show()
    }
}

// Whatsapp Trigger Method
fun openWhatsapp(context: Context, number: String) {
    try {
        val cleanNumber = number.replace("+", "").replace(" ", "")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "الواتساب غير مثبت أو غير مدعوم على هاتفك", Toast.LENGTH_SHORT).show()
    }
}

// ==========================================
// CUSTOM OVERLAY DIALOGS & OVERRIDES
// ==========================================
@Composable
fun NotificationCenterDialog(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مركز الإشعارات والتنبيهات المباشرة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ShanghaiBlue
                )
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("مسح الكل", color = ShanghaiPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد إشعارات جديدة متوفرة حالياً",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notif ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.6f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ShanghaiBlue
                                        )
                                        Text(
                                            text = notif.date,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.body,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue)
            ) {
                Text("تم", color = Color.White)
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp)
    )
}

@Composable
fun HeadsUpNotificationBanner(
    notification: AppNotification,
    onDismiss: () -> Unit
) {
    // Dismiss/Auto close after 6 seconds
    LaunchedEffect(notification.id) {
        delay(6000)
        onDismiss()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onDismiss)
            .testTag("heads_up_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShanghaiBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = ShanghaiGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "إشعار عاجل: ${notification.title}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = notification.body,
                    color = Color.White.copy(0.9f),
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
            }
        }
    }
}

@Composable
fun InquiryDialog(
    serviceTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var extraDetails by remember { mutableStateOf("") }
    var isInputValid by remember { mutableStateOf(false) }

    LaunchedEffect(clientName, clientPhone) {
        isInputValid = clientName.isNotBlank() && clientPhone.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "طلب استفسار وحجز لخدمة:\n$serviceTitle",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ShanghaiBlue,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "يرجى توفير معلومات الاتصال وسيتصل بك أحد ممثلي الخدمة هاتفياً لتجهيز كاف المتطلبات.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("الاسم بالكامل") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inquiry_input_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShanghaiBlue)
                )

                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("رقم جوال واتصال سريع") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inquiry_input_phone"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShanghaiBlue)
                )

                OutlinedTextField(
                    value = extraDetails,
                    onValueChange = { extraDetails = it },
                    label = { Text("ملاحظات إضافية أو تاريخ الرحلة المفضل") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShanghaiBlue)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(clientName, clientPhone, extraDetails) },
                enabled = isInputValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShanghaiBlue,
                    disabledContainerColor = ShanghaiBlue.copy(0.4f)
                ),
                modifier = Modifier.testTag("inquiry_submit_btn")
            ) {
                Text("إرسال الطلب الهاتفي", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = ShanghaiPink)
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp)
    )
}

// ==========================================
// ADMIN AUTH SCREEN LAYER
// ==========================================
@Composable
fun AdminAuthScreen(
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    isError: Boolean,
    onAuthenticate: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_login_card"),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(ShanghaiBlue.copy(0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ShanghaiBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "لوحة التحكم الإدارية المحمية للشركة",
                    fontWeight = FontWeight.Bold,
                    color = ShanghaiBlue,
                    fontSize = 16.sp
                )

                Text(
                    text = "يرجى إدخال كلمة المرور السرية للأدمن للوصول لخدمات إدارة العروض وتحديثات الأخبار ورصد طلبات المسافرين.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = onPasswordChange,
                    label = { Text("كلمة المرور الإدارية") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_field"),
                    singleLine = true,
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShanghaiBlue,
                        errorBorderColor = ShanghaiPink
                    )
                )

                if (isError) {
                    Text(
                        "العذر، كلمة المرور المدخلة خاطئة!",
                        color = ShanghaiPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAuthenticate,
                        colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("admin_login_submit")
                    ) {
                        Text("تسجيل الدخول", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("إلغاء والعودة", color = ShanghaiBlue)
                    }
                }
            }
        }
    }
}

// ==========================================
// ADMIN DASHBOARD CORE PANEL
// ==========================================
@Composable
fun AdminPanelDashboard(
    viewModel: TravelViewModel,
    offers: List<Offer>,
    news: List<NewsItem>,
    services: List<ServiceItem>,
    sliderImages: List<SliderImage>,
    inquiries: List<CustomerInquiry>,
    faqs: List<FAQ>,
    settings: AppSettings?,
    onExit: () -> Unit
) {
    var adminActiveTab by remember { mutableStateOf("inquiries") } // inquiries, offers, news, services, faqs, settings, notif

    // Input dialog models
    var showAddOfferDialog by remember { mutableStateOf(false) }
    var showAddNewsDialog by remember { mutableStateOf(false) }
    var showAddFAQDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_panel_dashboard")
    ) {
        // TOP Admin Header Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Engineering, contentDescription = null, tint = ShanghaiBlue)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "لوحة إدارة شركة شنغهاي",
                        fontWeight = FontWeight.Bold,
                        color = ShanghaiBlue,
                        fontSize = 15.sp
                    )
                }

                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = ShanghaiPink),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("خروج الأدمن", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Horizontal Category Tab Bar for Admin Panel Components
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val adminTabs = listOf(
                Pair("inquiries", "طلبات ورسائل العملاء (${inquiries.size})"),
                Pair("offers", "إدارة العروض والخصومات"),
                Pair("news", "إدارة الأخبار والتنبيهات"),
                Pair("services", "إدارة وتعديل الخدمات"),
                Pair("faqs", "إدارة الأسئلة الشائعة"),
                Pair("settings", "إعدادات المكتب والروابط"),
                Pair("notif", "إرسال شعار عاجل (Push)")
            )

            adminTabs.forEach { (tabId, label) ->
                val isSelected = adminActiveTab == tabId
                Box(
                    modifier = Modifier
                        .clickable { adminActiveTab = tabId }
                        .background(
                            color = if (isSelected) ShanghaiBlue else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            color = if (isSelected) ShanghaiBlue else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else ShanghaiBlue,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content Switching
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            when (adminActiveTab) {
                "inquiries" -> AdminInquiriesTab(
                    inquiries = inquiries,
                    onUpdateStatus = { id, status -> viewModel.updateInquiryStatus(id, status) },
                    onDelete = { inq -> viewModel.deleteInquiry(inq) }
                )
                "offers" -> AdminOffersTab(
                    offers = offers,
                    onAddRequested = { showAddOfferDialog = true },
                    onDelete = { id -> viewModel.deleteOffer(id) }
                )
                "news" -> AdminNewsTab(
                    news = news,
                    onAddRequested = { showAddNewsDialog = true },
                    onDelete = { id -> viewModel.deleteNews(id) }
                )
                "services" -> AdminServicesTab(
                    services = services,
                    onUpdateServiceVisibility = { updatedSrv -> viewModel.updateService(updatedSrv) }
                )
                "faqs" -> AdminFaqsTab(
                    faqs = faqs,
                    onAddRequested = { showAddFAQDialog = true },
                    onDelete = { id -> viewModel.deleteFAQ(id) }
                )
                "settings" -> AdminSettingsTab(
                    settings = settings,
                    onSave = { updatedSettings -> viewModel.updateSettings(updatedSettings) }
                )
                "notif" -> AdminNotifTab(
                    onSendTriggered = { title, body ->
                        viewModel.sendPushNotificationToAll(title, body)
                    }
                )
            }
        }

        // Sub-dialogs layer for Add Operations
        if (showAddOfferDialog) {
            AddOfferDialog(
                onDismiss = { showAddOfferDialog = false },
                onAdd = { offer ->
                    viewModel.addOffer(offer)
                    showAddOfferDialog = false
                }
            )
        }

        if (showAddNewsDialog) {
            AddNewsDialog(
                onDismiss = { showAddNewsDialog = false },
                onAdd = { newsItem ->
                    viewModel.addNews(newsItem)
                    showAddNewsDialog = false
                }
            )
        }

        if (showAddFAQDialog) {
            AddFAQDialog(
                onDismiss = { showAddFAQDialog = false },
                onAdd = { faq ->
                    viewModel.addFAQ(faq)
                    showAddFAQDialog = false
                }
            )
        }
    }
}

// ----------------------
// ADMIN TAB: INQUIRIES LIST
// ----------------------
@Composable
fun AdminInquiriesTab(
    inquiries: List<CustomerInquiry>,
    onUpdateStatus: (Int, String) -> Unit,
    onDelete: (CustomerInquiry) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("الكل") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث باسم العميل أو جواله..", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1.5f).height(48.dp),
                singleLine = true,
                textStyle = TextStyle(fontSize = 11.sp)
            )

            // Status filter spinner replacement
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("الكل", "Pending", "Approved", "Contacted").forEach { st ->
                    val selected = filterStatus == st
                    val readableText = when (st) {
                        "Pending" -> "معلّق"
                        "Approved" -> "مؤكد"
                        "Contacted" -> "تم الاتصال"
                        else -> "الكل"
                    }
                    Box(
                        modifier = Modifier
                            .clickable { filterStatus = st }
                            .background(
                                color = if (selected) ShanghaiPink else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text(readableText, color = if (selected) Color.White else ShanghaiBlue, fontSize = 9.sp)
                    }
                }
            }
        }

        val filteredInquiries = inquiries.filter { inq ->
            val matchesSearch = inq.clientName.contains(searchQuery, ignoreCase = true) ||
                    inq.clientPhone.contains(searchQuery) ||
                    inq.serviceType.contains(searchQuery, ignoreCase = true)
            val matchesStatus = filterStatus == "الكل" || inq.status == filterStatus
            matchesSearch && matchesStatus
        }

        if (filteredInquiries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد طلبات عملاء مطابقة للتصفية", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredInquiries) { inq ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = inq.clientName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ShanghaiBlue
                                )
                                // Interactive status switcher
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (inq.status) {
                                                "Approved" -> Color(0xFF4CAF50).copy(0.12f)
                                                "Contacted" -> Color(0xFF2196F3).copy(0.12f)
                                                else -> ShanghaiPink.copy(0.12f)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when (inq.status) {
                                            "Approved" -> "مؤكد ومقبول"
                                            "Contacted" -> "تم الاتصال به"
                                            else -> "طلب معلّق"
                                        },
                                        color = when (inq.status) {
                                            "Approved" -> Color(0xFF388E3C)
                                            "Contacted" -> Color(0xFF1976D2)
                                            else -> ShanghaiPink
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("الخدمة المطلوبة: ${inq.serviceType}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShanghaiPink)
                            Text("رقم العميل: ${inq.clientPhone}", fontSize = 12.sp)
                            Text("ملاحظات الطلب: ${inq.details}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TextButton(onClick = { onUpdateStatus(inq.id, "Approved") }) {
                                        Text("توليد كـ مقبول", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                    }
                                    TextButton(onClick = { onUpdateStatus(inq.id, "Contacted") }) {
                                        Text("تحديد كمقروء", fontSize = 10.sp, color = Color(0xFF2196F3))
                                    }
                                }

                                IconButton(onClick = { onDelete(inq) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ShanghaiPink, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------
// ADMIN TAB: OFFERS LIST MANAGER
// ----------------------
@Composable
fun AdminOffersTab(
    offers: List<Offer>,
    onAddRequested: () -> Unit,
    onDelete: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onAddRequested,
            colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة عرض ترويجي جديد", color = Color.White, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(offers) { offer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(offer.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShanghaiBlue)
                            Text(offer.description, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("مميز: ${if (offer.isFeatured) "نعم" else "لا"}", fontSize = 10.sp, color = ShanghaiPink)
                                Text("التاريخ: ${offer.startDate} إلى ${offer.endDate}", fontSize = 10.sp)
                            }
                        }

                        IconButton(onClick = { onDelete(offer.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ShanghaiPink)
                        }
                    }
                }
            }
        }
    }
}

// Dialog to add customized offers
@Composable
fun AddOfferDialog(onDismiss: () -> Unit, onAdd: (Offer) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var isFeatured by remember { mutableStateOf(false) }
    var start by remember { mutableStateOf("09-06-2026") }
    var end by remember { mutableStateOf("30-06-2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة عرض وخصم ترويجي مميز", fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان العرض") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("وصف مختصر وشامل للعرض") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("تاريخ بداية العرض") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("تاريخ نهاية العرض") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تمميز وتثبيت العرض كـ مميز")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            Offer(
                                title = title,
                                description = desc,
                                imageUrl = "offer_custom",
                                startDate = start,
                                endDate = end,
                                isFeatured = isFeatured,
                                isActive = true
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue)
            ) { Text("حفظ", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = ShanghaiPink) }
        }
    )
}

// ----------------------
// ADMIN TAB: NEWS MANAGER
// ----------------------
@Composable
fun AdminNewsTab(
    news: List<NewsItem>,
    onAddRequested: () -> Unit,
    onDelete: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onAddRequested,
            colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة إعلان أو خبر عاجل", color = Color.White, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(news) { newsItem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(newsItem.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShanghaiBlue)
                            Text(newsItem.content, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("تاريخ النشر: ${newsItem.date}", fontSize = 10.sp)
                                if (newsItem.isImportant) {
                                    Text("مهم وعاجل", fontSize = 10.sp, color = ShanghaiPink, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        IconButton(onClick = { onDelete(newsItem.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ShanghaiPink)
                        }
                    }
                }
            }
        }
    }
}

// Dialog to add customized news
@Composable
fun AddNewsDialog(onDismiss: () -> Unit, onAdd: (NewsItem) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نشر خبر وإعلان جديد", fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الخبر") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("توصيف تفصيلي للخبر") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isImportant, onCheckedChange = { isImportant = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ضع علامة كخبر عاجل ومهم بالتطبيق")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            NewsItem(
                                title = title,
                                content = content,
                                imageUrl = "news_custom",
                                date = "09-06-2026",
                                isImportant = isImportant
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue)
            ) { Text("نشر فوري", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = ShanghaiPink) }
        }
    )
}

// ----------------------
// ADMIN TAB: SERVICES SHOW / HIDE
// ----------------------
@Composable
fun AdminServicesTab(
    services: List<ServiceItem>,
    onUpdateServiceVisibility: (ServiceItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Box(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    "إدارة إظهار أو إخفاء الخدمات السياحية وتعديلها",
                    color = ShanghaiBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(services) { srv ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(srv.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShanghaiBlue)
                        Text(srv.description, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (srv.isVisible) "ظاهرة بالتطبيق" else "مخفية",
                            fontSize = 11.sp,
                            color = if (srv.isVisible) Color(0xFF4CAF50) else ShanghaiPink
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = srv.isVisible,
                            onCheckedChange = { isChecked ->
                                onUpdateServiceVisibility(srv.copy(isVisible = isChecked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ShanghaiBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = ShanghaiPink
                            )
                        )
                    }
                }
            }
        }
    }
}

// ----------------------
// ADMIN TAB: FAQS MANAGER
// ----------------------
@Composable
fun AdminFaqsTab(
    faqs: List<FAQ>,
    onAddRequested: () -> Unit,
    onDelete: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onAddRequested,
            colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة سؤال شائعة جديد", color = Color.White, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(faqs) { faq ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(faq.question, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShanghaiBlue)
                            Text(faq.answer, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        IconButton(onClick = { onDelete(faq.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ShanghaiPink)
                        }
                    }
                }
            }
        }
    }
}

// Dialog to add customized FAQs
@Composable
fun AddFAQDialog(onDismiss: () -> Unit, onAdd: (FAQ) -> Unit) {
    var q by remember { mutableStateOf("") }
    var a by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إدراج سؤال شائعة", fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = q, onValueChange = { q = it }, label = { Text("السؤال المقترح") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("الإجابة المعتمدة") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (q.isNotBlank() && a.isNotBlank()) {
                        onAdd(FAQ(question = q, answer = a))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue)
            ) { Text("تم الرفع", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = ShanghaiPink) }
        }
    )
}

// ----------------------
// ADMIN TAB: APPLICATION SETTINGS
// ----------------------
@Composable
fun AdminSettingsTab(
    settings: AppSettings?,
    onSave: (AppSettings) -> Unit
) {
    var p1 by remember { mutableStateOf(settings?.phone1 ?: "737103025") }
    var p2 by remember { mutableStateOf(settings?.phone2 ?: "738601113") }
    var land by remember { mutableStateOf(settings?.landline ?: "01688617") }
    var email by remember { mutableStateOf(settings?.email ?: "info@shanghaitravel.net") }
    var wa by remember { mutableStateOf(settings?.whatsapp ?: "+967737103025") }
    var address by remember { mutableStateOf(settings?.officeAddress ?: "صنعاء - مجمع الشلال - الدور السادس") }
    var exp by remember { mutableStateOf((settings?.yearsOfExperience ?: 15).toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text("تحيين وتعديل معلومات وبيانات الاتصال والروابط الرسمية للشاشات:", color = ShanghaiBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            OutlinedTextField(value = p1, onValueChange = { p1 = it }, label = { Text("هاتف الاتصال الرئيسي") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = p2, onValueChange = { p2 = it }, label = { Text("هاتف الاتصال الاحتياطي") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = land, onValueChange = { land = it }, label = { Text("تلفون الهاتف الأرضي") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = wa, onValueChange = { wa = it }, label = { Text("رقم الواتساب بالترميز الدولي") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("البريد الإلكتروني للشركة") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("العنوان الرئيسي الجغرافي") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("سنوات الخبرة للمكتب") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }

        item {
            Button(
                onClick = {
                    settings?.let {
                        onSave(
                            it.copy(
                                phone1 = p1,
                                phone2 = p2,
                                landline = land,
                                email = email,
                                whatsapp = wa,
                                officeAddress = address,
                                yearsOfExperience = exp.toIntOrNull() ?: 15
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue),
                modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("حفظ وتحديث كاف التعديلات بالتطبيق", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------------
// ADMIN TAB: TRIGGER SIMULATED NOTIFICATIONS (PUSH)
// ----------------------
@Composable
fun AdminNotifTab(
    onSendTriggered: (String, String) -> Unit
) {
    var pushTitle by remember { mutableStateOf("") }
    var pushBody by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_notif_tab"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                "نظام إرسال الإشعارات والتنبيهات المباشرة لجميع مستخدمي التطبيق",
                color = ShanghaiBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = pushTitle,
                    onValueChange = { pushTitle = it },
                    label = { Text("عنوان التنبيه أو العرض") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pushBody,
                    onValueChange = { pushBody = it },
                    label = { Text("محتوى وتفاصيل رسالة الإشعار") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 3
                )

                Button(
                    onClick = {
                        if (pushTitle.isNotBlank() && pushBody.isNotBlank()) {
                            onSendTriggered(pushTitle, pushBody)
                            pushTitle = ""
                            pushBody = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShanghaiBlue),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = pushTitle.isNotBlank() && pushBody.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إرسال الإشعار لجميع الهواتف الآن", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
