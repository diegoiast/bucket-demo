package com.example.bucketexample

interface ValueProcessor {
    suspend fun init()
    suspend fun processValue(value: String)
    suspend fun flushRemainingValues(): List<List<Double>>
    fun getAllProcessedTrips(): List<List<Double>>
    fun getItemsCount(): Int
}