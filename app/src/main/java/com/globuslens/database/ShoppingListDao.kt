package com.globuslens.database

import androidx.room.*
import com.globuslens.database.entities.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Insert
    suspend fun insertShoppingItem(item: ShoppingItem): Long

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)

    @Query("SELECT * FROM shopping_list ORDER BY isChecked ASC, category ASC")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_list WHERE isChecked = 0 ORDER BY category ASC")
    fun getActiveShoppingItems(): Flow<List<ShoppingItem>>

    @Query("UPDATE shopping_list SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun updateCheckStatus(itemId: Long, isChecked: Boolean)

    @Query("DELETE FROM shopping_list WHERE isChecked = 1")
    suspend fun deleteCheckedItems()

    @Query("UPDATE shopping_list SET quantity = :quantity WHERE id = :itemId")
    suspend fun updateQuantity(itemId: Long, quantity: Int)
}