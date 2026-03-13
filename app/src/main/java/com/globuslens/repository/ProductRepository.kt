package com.globuslens.repository

import com.globuslens.database.ProductDao
import com.globuslens.database.entities.Product
import com.globuslens.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {

    fun getAllProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        productDao.getAllProducts().collect { products ->
            emit(Resource.Success(products))
        }
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Unknown error"))
    }

    fun getFavoriteProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        productDao.getFavoriteProducts().collect { products ->
            emit(Resource.Success(products))
        }
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Unknown error"))
    }

    fun getProductById(productId: Long): Flow<Resource<Product?>> = flow {
        emit(Resource.Loading())
        productDao.getProductById(productId).collect { product ->
            emit(Resource.Success(product))
        }
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Unknown error"))
    }

    suspend fun saveProduct(product: Product): Resource<Long> {
        return try {
            val id = productDao.insertProduct(product)
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save product")
        }
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return try {
            productDao.getProductByBarcode(barcode)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProduct(product: Product): Resource<Unit> {
        return try {
            productDao.updateProduct(product)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update product")
        }
    }

    suspend fun deleteProduct(product: Product): Resource<Unit> {
        return try {
            productDao.deleteProduct(product)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete product")
        }
    }

    suspend fun toggleFavorite(productId: Long, isFavorite: Boolean): Resource<Unit> {
        return try {
            productDao.updateFavoriteStatus(productId, isFavorite)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update favorite status")
        }
    }
}