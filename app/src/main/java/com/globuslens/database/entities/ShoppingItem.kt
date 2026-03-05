package com.globuslens.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "shopping_list")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long? = null,
    val name: String,
    val quantity: Int = 1,
    val price: Double? = null,
    val isChecked: Boolean = false,
    val notes: String? = null,
    val category: String? = null
) : Parcelable