package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offers")
data class Offer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imageUrl: String,
    val startDate: String,
    val endDate: String,
    val isFeatured: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "news")
data class NewsItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val imageUrl: String,
    val date: String,
    val isImportant: Boolean = false
)

@Entity(tableName = "services")
data class ServiceItem(
    @PrimaryKey val id: String, // e.g. "flights", "hotels", "visas", "hajj_umrah", "yemen_transport", "intl_transport", "tourism_programs", "trips", "corporate"
    val title: String,
    val description: String,
    val features: String, // Delimiter-separated list (e.g. "Feature A\nFeature B")
    val imageUrl: String,
    val isVisible: Boolean = true
)

@Entity(tableName = "slider_images")
data class SliderImage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUrl: String,
    val caption: String
)

@Entity(tableName = "customer_inquiries")
data class CustomerInquiry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val clientPhone: String,
    val serviceType: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending" // Pending, Approved, Contacted, Rejected
)

@Entity(tableName = "faqs")
data class FAQ(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val officeAddress: String,
    val phone1: String,
    val phone2: String,
    val landline: String,
    val email: String,
    val whatsapp: String,
    val snapchat: String,
    val facebook: String,
    val instagram: String,
    val aboutUsText: String,
    val visionText: String,
    val missionText: String,
    val valuesText: String,
    val yearsOfExperience: Int
)
