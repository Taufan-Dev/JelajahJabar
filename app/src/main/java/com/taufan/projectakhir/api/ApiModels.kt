package com.taufan.projectakhir.api

import com.google.gson.annotations.SerializedName

// --- Register Models ---
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// Format response API register:
// {"status":"success","message":"...","data":{"user":{...},"token":"..."}}
// atau error: {"status":"error","errors":{"password":["..."]}}
data class RegisterResponse(
    val status: String?,
    val message: String?,
    val data: AuthData?,
    val errors: Map<String, List<String>>? // untuk validasi error dari Laravel
)

// --- Login Models ---
data class LoginRequest(
    val email: String,
    val password: String
)

// Format response API login:
// {"status":"success","message":"...","data":{"user":{...},"token":"..."}}
data class LoginResponse(
    val status: String?,
    val message: String?,
    val data: AuthData?
)

// Data gabungan untuk login & register (keduanya punya user + token di dalam "data")
data class AuthData(
    val user: UserData?,
    val token: String?
)

// --- Shared User Data ---
data class UserData(
    val id: Int?,
    val name: String?,
    val email: String?,
    @SerializedName("id_wilayah") val idWilayah: Int?,
    val role: String?
)

// --- Wisata Models ---
data class WisataResponse(
    val status: String,
    val message: String,
    val data: List<WisataApiItem>
)

data class WisataApiItem(
    val id: Int,
    @SerializedName("nama_wisata") val namaWisata: String,
    val deskripsi: String,
    val lokasi: String,
    @SerializedName("harga_tiket") val hargaTiket: String,
    val status: String,
    val kategori: String?, // Kategori baru
    val gambar: com.google.gson.JsonElement?, // Menggunakan JsonElement agar super aman (string atau json array)
    @SerializedName("total_terjual") val totalTerjual: Int,
    @SerializedName("rata_rating") val rataRating: Double?,
    val wilayah: WilayahData?,
    val pengelola: UserData?
)

data class WilayahData(
    val id: Int,
    @SerializedName("nama_kabupaten") val namaKabupaten: String
)
