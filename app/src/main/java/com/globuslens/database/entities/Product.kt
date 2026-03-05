package com.globuslens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val translatedName: String? = null,
    val price: Double? = null,
    val currency: String = "USD",
    val category: String? = null,
    val nutritionInfo: String? = null,
    val ingredients: String? = null,
    val allergens: String? = null,
    val expiryDate: Date? = null,
    val imageUrl: String? = null,
    val barcode: String? = null,
    val isFavorite: Boolean = false,
    val scannedDate: Date = Date(),
    val originalLanguage: String? = null
) : Parcelable