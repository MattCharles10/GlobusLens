package com.globuslens.repository

import com.globuslens.database.ShoppingListDao
import com.globuslens.database.entities.ShoppingItem
import com.globuslens.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val shoppingListDao: ShoppingListDao
) {

    fun getAllShoppingItems(): Flow<Resource<List<ShoppingItem>>> = flow {
        emit(Resource.Loading())
        shoppingListDao.getAllShoppingItems().collect { items ->
            emit(Resource.Success(items))
        }
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Unknown error"))
    }

    fun getActiveShoppingItems(): Flow<Resource<List<ShoppingItem>>> = flow {
        emit(Resource.Loading())
        shoppingListDao.getActiveShoppingItems().collect { items ->
            emit(Resource.Success(items))
        }
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Unknown error"))
    }

    suspend fun addShoppingItem(item: ShoppingItem): Resource<Long> {
        return try {
            val id = shoppingListDao.insertShoppingItem(item)
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add item")
        }
    }

    suspend fun updateShoppingItem(item: ShoppingItem): Resource<Unit> {
        return try {
            shoppingListDao.updateShoppingItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update item")
        }
    }

    suspend fun deleteShoppingItem(item: ShoppingItem): Resource<Unit> {
        return try {
            shoppingListDao.deleteShoppingItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete item")
        }
    }

    suspend fun toggleItemChecked(itemId: Long, isChecked: Boolean): Resource<Unit> {
        return try {
            shoppingListDao.updateCheckStatus(itemId, isChecked)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update item status")
        }
    }

    suspend fun updateQuantity(itemId: Long, quantity: Int): Resource<Unit> {
        return try {
            shoppingListDao.updateQuantity(itemId, quantity)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update quantity")
        }
    }

    suspend fun clearCheckedItems(): Resource<Unit> {
        return try {
            shoppingListDao.deleteCheckedItems()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to clear checked items")
        }
    }
}