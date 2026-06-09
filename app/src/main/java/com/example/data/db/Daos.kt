package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDao {
    // Offers
    @Query("SELECT * FROM offers ORDER BY id DESC")
    fun getAllOffersFlow(): Flow<List<Offer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: Offer)

    @Update
    suspend fun updateOffer(offer: Offer)

    @Delete
    suspend fun deleteOffer(offer: Offer)

    @Query("DELETE FROM offers WHERE id = :id")
    suspend fun deleteOfferById(id: Int)

    // News
    @Query("SELECT * FROM news ORDER BY date DESC, id DESC")
    fun getAllNewsFlow(): Flow<List<NewsItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(newsItem: NewsItem)

    @Update
    suspend fun updateNews(newsItem: NewsItem)

    @Delete
    suspend fun deleteNews(newsItem: NewsItem)

    @Query("DELETE FROM news WHERE id = :id")
    suspend fun deleteNewsById(id: Int)

    // Services
    @Query("SELECT * FROM services")
    fun getAllServicesFlow(): Flow<List<ServiceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceItem)

    @Update
    suspend fun updateService(service: ServiceItem)

    @Delete
    suspend fun deleteService(service: ServiceItem)

    // Slider Images
    @Query("SELECT * FROM slider_images ORDER BY id DESC")
    fun getAllSliderImagesFlow(): Flow<List<SliderImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSliderImage(sliderImage: SliderImage)

    @Delete
    suspend fun deleteSliderImage(sliderImage: SliderImage)

    @Query("DELETE FROM slider_images WHERE id = :id")
    suspend fun deleteSliderImageById(id: Int)

    // Inquiries
    @Query("SELECT * FROM customer_inquiries ORDER BY timestamp DESC")
    fun getAllInquiriesFlow(): Flow<List<CustomerInquiry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: CustomerInquiry)

    @Update
    suspend fun updateInquiry(inquiry: CustomerInquiry)

    @Query("UPDATE customer_inquiries SET status = :status WHERE id = :id")
    suspend fun updateInquiryStatus(id: Int, status: String)

    @Delete
    suspend fun deleteInquiry(inquiry: CustomerInquiry)

    // FAQs
    @Query("SELECT * FROM faqs ORDER BY id ASC")
    fun getAllFAQsFlow(): Flow<List<FAQ>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFAQ(faq: FAQ)

    @Update
    suspend fun updateFAQ(faq: FAQ)

    @Delete
    suspend fun deleteFAQ(faq: FAQ)

    @Query("DELETE FROM faqs WHERE id = :id")
    suspend fun deleteFAQById(id: Int)

    // Settings
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
}
