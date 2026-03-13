package com.globuslens.utils

import com.globuslens.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeLookup @Inject constructor() {

    data class ProductInfo(
        val productName: String,
        val category: String? = null,
        val brand: String? = null,
        val imageUrl: String? = null,
        val description: String? = null
    )

    suspend fun lookupBarcode(barcode: String): Resource<ProductInfo> = withContext(Dispatchers.IO) {
        // Try multiple free barcode APIs in order of reliability
        val apis = listOf(
            { lookupOpenFoodFacts(barcode) },  // Most reliable for food products
            { lookupUpcItemDb(barcode) },       // General products
            { lookupBarcodeList(barcode) }      // Backup API
        )

        for (api in apis) {
            try {
                val result = api.invoke()
                if (result != null) {
                    return@withContext Resource.Success(result)
                }
            } catch (e: Exception) {
                // Log error but continue to next API
                e.printStackTrace()
            }
        }

        return@withContext Resource.Error("Product not found in any database")
    }

    private fun lookupOpenFoodFacts(barcode: String): ProductInfo? {
        return try {
            val url = URL("https://world.openfoodfacts.org/api/v2/product/$barcode.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "GlobusLens-Android/1.0")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                if (json.getString("status") == "1") {
                    val product = json.getJSONObject("product")
                    val name = product.optString("product_name").takeIf { it.isNotBlank() }
                        ?: product.optString("generic_name").takeIf { it.isNotBlank() }
                        ?: "Unknown Product"

                    val brands = product.optString("brands").takeIf { it.isNotBlank() }
                    val categories = product.optString("categories").takeIf { it.isNotBlank() }
                    val image = product.optString("image_url").takeIf { it.isNotBlank() }
                        ?: product.optString("image_front_url").takeIf { it.isNotBlank() }

                    val ingredients = product.optString("ingredients_text").takeIf { it.isNotBlank() }

                    return ProductInfo(
                        productName = name,
                        category = categories?.split(',')?.firstOrNull()?.trim(),
                        brand = brands,
                        imageUrl = image,
                        description = ingredients
                    )
                }
            }
            connection.disconnect()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun lookupUpcItemDb(barcode: String): ProductInfo? {
        return try {
            val url = URL("https://api.upcitemdb.com/prod/trial/lookup?upc=$barcode")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "GlobusLens-Android/1.0")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                if (json.getInt("code") == 200) {
                    val items = json.getJSONArray("items")
                    if (items.length() > 0) {
                        val item = items.getJSONObject(0)
                        val title = item.getString("title")
                        val brand = item.optString("brand").takeIf { it.isNotBlank() }
                        val category = item.optString("category").takeIf { it.isNotBlank() }
                        val image = item.optJSONArray("images")?.optString(0).takeIf { !it.isNullOrBlank() }
                        val description = item.optString("description").takeIf { it.isNotBlank() }

                        return ProductInfo(
                            productName = title,
                            category = category,
                            brand = brand,
                            imageUrl = image,
                            description = description
                        )
                    }
                }
            }
            connection.disconnect()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun lookupBarcodeList(barcode: String): ProductInfo? {
        return try {
            val url = URL("https://barcode-list.com/barcode/$barcode.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "GlobusLens-Android/1.0")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                if (json.has("product")) {
                    val product = json.getJSONObject("product")
                    val name = product.optString("name").takeIf { it.isNotBlank() }
                        ?: product.optString("title").takeIf { it.isNotBlank() }
                        ?: "Unknown Product"

                    val brand = product.optString("brand").takeIf { it.isNotBlank() }
                    val category = product.optString("category").takeIf { it.isNotBlank() }
                    val image = product.optString("image").takeIf { it.isNotBlank() }

                    return ProductInfo(
                        productName = name,
                        category = category,
                        brand = brand,
                        imageUrl = image
                    )
                }
            }
            connection.disconnect()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper function to safely read response
    private fun HttpURLConnection.getResponseText(): String? {
        return try {
            val inputStream = if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream
            } else {
                errorStream
            }

            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                response.toString()
            }
        } catch (e: Exception) {
            null
        } finally {
            disconnect()
        }
    }
}