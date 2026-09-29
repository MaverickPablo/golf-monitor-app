package com.golfmonitor.data.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class UkGolfClubDto(
    val id: String,
    val name: String,
    val county: String?,
    val latitude: Double,
    val longitude: Double,
    val postcode: String?,
    val greenFeeBaseline: Double?
)

data class NearbyClubsResponse(
    val clubs: List<UkGolfClubDto>
)

interface UkGolfApiService {
    @GET("clubs/nearby")
    suspend fun getNearbyClubs(
        @Header("X-RapidAPI-Key") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius_km") radiusKm: Int
    ): NearbyClubsResponse
}
