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
    val pengelola: UserData?,
    @SerializedName("rekomendasis") val rekomendasis: List<RekomendasiData>? = null
)

data class WilayahData(
    val id: Int,
    @SerializedName("nama_kabupaten") val namaKabupaten: String
)

// --- Ticket / Transaction Models ---
data class TicketRequest(
    @SerializedName("wisata_id") val wisataId: Int,
    @SerializedName("jumlah_tiket") val jumlahTiket: Int,
    @SerializedName("tanggal_kunjungan") val tanggalKunjungan: String
)

data class TicketResponse(
    val status: String?,
    val message: String?,
    val data: TicketData?
)

data class TicketData(
    val id: Int?,
    @SerializedName("kode_tiket") val kodeTiket: String?,
    @SerializedName("wisata_id") val wisataId: Int?,
    @SerializedName("jumlah_tiket") val jumlahTiket: Int?,
    @SerializedName("tanggal_kunjungan") val tanggalKunjungan: String?,
    @SerializedName("total_bayar") val totalBayar: Double?,
    @SerializedName("status_pembayaran") val status: String?,
    @SerializedName("snap_token") val snapToken: String?
)

data class TicketHistoryResponse(
    val status: String?,
    val message: String?,
    val data: List<TicketHistoryItem>?
)

data class TicketHistoryItem(
    val id: Int?,
    @SerializedName("kode_tiket") val kodeTiket: String?,
    @SerializedName("wisata_id") val wisataId: Int?,
    @SerializedName("jumlah_tiket") val jumlahTiket: Int?,
    @SerializedName("tanggal_kunjungan") val tanggalKunjungan: String?,
    @SerializedName("total_bayar") val totalBayar: Double?,
    @SerializedName("status_pembayaran") val status: String?,
    @SerializedName("status_tiket") val statusTiket: String?,
    @SerializedName("snap_token") val snapToken: String?,
    val wisata: WisataApiItem?
)

// --- Simulate Callback Models ---
data class SimulateCallbackRequest(
    @SerializedName("kode_tiket") val kodeTiket: String,
    val status: String = "settlement"
)

data class SimulateCallbackResponse(
    val status: String?,
    val message: String?
)

// --- Profile Response ---
data class UserProfileResponse(
    val status: String?,
    val message: String?,
    val data: UserData?
)

// --- Generic Status Response ---
data class SimpleResponse(
    val status: String?,
    val message: String?
)

// --- Ulasan / Rekomendasi Models ---
data class WisataDetailResponse(
    val status: String?,
    val message: String?,
    val data: WisataApiItem?
)

data class RekomendasiData(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("wisata_id") val wisataId: Int,
    val rating: Int,
    val ulasan: String?,
    @SerializedName("created_at") val createdAt: String?,
    val user: UserData?,
    val gambar: com.google.gson.JsonElement? = null
)

data class RekomendasiRequest(
    @SerializedName("wisata_id") val wisataId: Int,
    val rating: Int,
    val ulasan: String
)
