package com.golfmonitor.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * UK Golf Course API on RapidAPI (golfapi.uk). Shapes follow its openapi.json,
 * documented at github.com/RyanNuttall/uk-golf-course-api-examples.
 * Free plan: 200 requests a month, so the app uses data bundled in courses.json
 * and only calls this when refreshing it.
 */
data class UkGolfClubDto(
    val id: String,
    val name: String,
    val city: String?,
    val county: String?,
    val postcode: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("google_rating") val googleRating: Double?,
    @SerializedName("course_type") val courseType: String?,
    @SerializedName("distance_miles") val distanceMiles: Double?
)

data class NearbyClubsResponse(
    val total: Int,
    val clubs: List<UkGolfClubDto>
)

data class GreenFeeDto(
    @SerializedName("fee_type") val feeType: String?,
    val holes: Int?,
    @SerializedName("price_gbp") val priceGbp: Double?,
    val notes: String?
)

interface UkGolfApiService {
    /** Returns at most 50 clubs (API limits: radius_miles <= 50, per_page <= 50). */
    @GET("clubs/nearby")
    suspend fun getNearbyClubs(
        @Header("X-RapidAPI-Key") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius_miles") radiusMiles: Int,
        @Query("per_page") perPage: Int = 50
    ): NearbyClubsResponse

    @GET("clubs/{id}/green-fees")
    suspend fun getGreenFees(
        @Header("X-RapidAPI-Key") apiKey: String,
        @Path("id") clubId: String
    ): List<GreenFeeDto>
}
