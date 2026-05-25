package com.taufan.projectakhir.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @Headers("Accept: application/json")
    @POST("api/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @Headers("Accept: application/json")
    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @Headers("Accept: application/json")
    @GET("api/wisata")
    fun getWisata(
        @Query("id_wilayah") idWilayah: Int? = null,
        @Query("search") search: String? = null
    ): Call<WisataResponse>
}
