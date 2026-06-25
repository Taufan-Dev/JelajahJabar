package com.taufan.projectakhir.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {

    @Headers("Accept: application/json")
    @POST("api/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @Headers("Accept: application/json")
    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @Headers("Accept: application/json")
    @POST("api/logout")
    fun logout(@Header("Authorization") token: String): Call<SimpleResponse>

    @Headers("Accept: application/json")
    @GET("api/me")
    fun getProfile(@Header("Authorization") token: String): Call<UserProfileResponse>

    @Headers("Accept: application/json")
    @GET("api/wisata")
    fun getWisata(
        @Query("id_wilayah") idWilayah: Int? = null,
        @Query("kategori") kategori: String? = null,
        @Query("search") search: String? = null
    ): Call<WisataResponse>

    @Headers("Accept: application/json")
    @POST("api/tiket")
    fun bookTicket(
        @Header("Authorization") token: String,
        @Body request: TicketRequest
    ): Call<TicketResponse>

    @Headers("Accept: application/json")
    @GET("api/tiket")
    fun getTicketHistory(@Header("Authorization") token: String): Call<TicketHistoryResponse>

    @Headers("Accept: application/json")
    @POST("api/payment/simulate-callback")
    fun simulateCallback(@Body request: SimulateCallbackRequest): Call<SimulateCallbackResponse>

    @Headers("Accept: application/json")
    @GET("api/wisata/{id}")
    fun getWisataDetail(@Path("id") id: Int): Call<WisataDetailResponse>

    @Headers("Accept: application/json")
    @POST("api/rekomendasi")
    fun submitReview(
        @Header("Authorization") token: String,
        @Body request: RekomendasiRequest
    ): Call<SimpleResponse>

    @Multipart
    @POST("api/rekomendasi")
    fun submitReviewMultipart(
        @Header("Authorization") token: String,
        @Part("wisata_id") wisataId: RequestBody,
        @Part("rating") rating: RequestBody,
        @Part("ulasan") ulasan: RequestBody,
        @Part gambar: List<MultipartBody.Part>? = null
    ): Call<SimpleResponse>
}
