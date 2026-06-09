package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.repository.TravelRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppNotification(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val body: String,
    val date: String,
    val isRead: Boolean = false
)

class TravelViewModel(application: Application, private val repository: TravelRepository) : AndroidViewModel(application) {

    // Notifications state list
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Toast/Heads-up notification events
    private val _headsUpNotification = MutableStateFlow<AppNotification?>(null)
    val headsUpNotification = _headsUpNotification.asStateFlow()

    val allOffers: StateFlow<List<Offer>> = repository.allOffers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNews: StateFlow<List<NewsItem>> = repository.allNews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServices: StateFlow<List<ServiceItem>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSliderImages: StateFlow<List<SliderImage>> = repository.allSliderImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInquiries: StateFlow<List<CustomerInquiry>> = repository.allInquiries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFAQs: StateFlow<List<FAQ>> = repository.allFAQs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettings?> = repository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulate()
            // Preset some initial notifications
            _notifications.value = listOf(
                AppNotification(
                    title = "مرحباً بكم في شنغهاي للسياحة",
                    body = "يسرنا انضمامكم إلينا! تابعوا أحدث عروض السفر والحجوزات بأرقى المستويات.",
                    date = "09-06-2026"
                ),
                AppNotification(
                    title = "انطلاق أسطول البر الفخم",
                    body = "تفقد عروض النقل البري الدولي الجديد إلى الرياض وجدة مع حافلات VIP الفخمة.",
                    date = "08-06-2026"
                )
            )
        }
    }

    // Submit inquiry from any service form or general contact form
    fun submitInquiry(name: String, phone: String, service: String, details: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.submitInquiry(name, phone, service, details)
            onSuccess()
        }
    }

    // Admin commands
    fun addOffer(offer: Offer) = viewModelScope.launch { repository.addOffer(offer) }
    fun updateOffer(offer: Offer) = viewModelScope.launch { repository.updateOffer(offer) }
    fun deleteOffer(id: Int) = viewModelScope.launch { repository.deleteOfferById(id) }

    fun addNews(newsItem: NewsItem) = viewModelScope.launch { repository.addNews(newsItem) }
    fun updateNews(newsItem: NewsItem) = viewModelScope.launch { repository.updateNews(newsItem) }
    fun deleteNews(id: Int) = viewModelScope.launch { repository.deleteNewsById(id) }

    fun updateService(serviceItem: ServiceItem) = viewModelScope.launch { repository.updateService(serviceItem) }

    fun addSliderImage(sliderImage: SliderImage) = viewModelScope.launch { repository.addSliderImage(sliderImage) }
    fun deleteSliderImage(id: Int) = viewModelScope.launch { repository.deleteSliderImageById(id) }

    fun addFAQ(faq: FAQ) = viewModelScope.launch { repository.addFAQ(faq) }
    fun updateFAQ(faq: FAQ) = viewModelScope.launch { repository.updateFAQ(faq) }
    fun deleteFAQ(id: Int) = viewModelScope.launch { repository.deleteFAQById(id) }

    fun updateInquiryStatus(id: Int, status: String) = viewModelScope.launch { repository.updateInquiryStatus(id, status) }
    fun deleteInquiry(inquiry: CustomerInquiry) = viewModelScope.launch { repository.deleteInquiry(inquiry) }

    fun updateSettings(settings: AppSettings) = viewModelScope.launch { repository.updateSettings(settings) }

    // Simulated Push Notification triggers
    fun sendPushNotificationToAll(title: String, body: String) {
        viewModelScope.launch {
            val dateStr = "09-06-2026" // Current date matching metadata logs
            val newNotif = AppNotification(
                title = title,
                body = body,
                date = dateStr
            )
            // Prepend new notification to user history
            _notifications.value = listOf(newNotif) + _notifications.value
            // Trigger heads up banner
            _headsUpNotification.value = newNotif
        }
    }

    fun dismissHeadsUp() {
        _headsUpNotification.value = null
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }
}
