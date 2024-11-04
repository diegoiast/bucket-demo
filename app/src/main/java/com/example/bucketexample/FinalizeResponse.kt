package com.example.bucketexample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinalizeResponse(
    @SerialName("item_count")
    val itemCount: Int,
    val trips: List<List<Double>>
)
