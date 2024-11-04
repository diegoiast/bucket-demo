package com.example.bucketexample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class RemoteValueProcessor(private val server: String) : ValueProcessor {
    private val client = OkHttpClient()
    private var trips : List<List<Double>> = mutableListOf()
    private var itemCount = 0

    companion object {
        const val V1 = "v1"
        const val V2 = "v2"
    }

    private var version = V1  // Default version


    fun setVersion(newVersion: String) {
        if (newVersion != V1 && newVersion != V2) {
            throw IllegalArgumentException("Invalid version. Use V1 or V2.")
        }
        version = newVersion
    }

    override suspend fun init() {
        withContext(Dispatchers.IO) {
            val url = "$server/init/$version"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to initialize processor: ${response.code} ${response.message}")
                }
            }
        }
    }

    override suspend fun processValue(value: String) {
        itemCount ++
        return withContext(Dispatchers.IO) {
            val url = "$server/add_number/$value"
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext
                }
                val jsonResponse = response.body?.string() ?: return@withContext
            }
        }
    }

    override suspend fun flushRemainingValues(): List<List<Double>> = withContext(Dispatchers.IO) {
        val serverUrl = "$server/finalize/"
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        itemCount = 0
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseBody = response.body?.string() ?: throw IOException("Empty response body")
                trips = Json.decodeFromString<FinalizeResponse>(responseBody).trips
                trips
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log or handle the exception as needed
            emptyList() // Return an empty list in case of error
        }
    }


    override fun getAllProcessedTrips(): List<List<Double>> {
        return trips
    }

    override fun getItemsCount(): Int {
        return itemCount
    }
}
