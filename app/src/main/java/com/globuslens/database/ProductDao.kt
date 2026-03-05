package com.globuslens.database

import androidx.room.*
import com.globuslens.database.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE isFavorite = 1 ORDER BY scannedDate DESC")
    fun getFavoriteProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY scannedDate DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :productId")
    suspend fun updateFavoriteStatus(productId: Long, isFavorite: Boolean)

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)
}