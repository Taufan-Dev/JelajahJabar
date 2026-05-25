package com.taufan.projectakhir

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.taufan.projectakhir.api.WisataApiItem

@Parcelize
data class Wisata(
    val nama: String,
    val lokasi: String,
    val gambar: Int,
    val kota: String,
    val harga: String,
    val deskripsi: String,
    val rating: String = "(5.0)",
    val gambarUrl: String? = null, // Gambar utama (pertama)
    val gambarUrls: List<String>? = null, // List maksimal 5 gambar
    val kategori: String? = null // Kategori wisata
) : Parcelable

// Extension function untuk memetakan WisataApiItem ke model Wisata lokal
fun WisataApiItem.toWisata(): Wisata {
    val localDrawable = when {
        namaWisata.contains("Kawah Putih", ignoreCase = true) -> R.drawable.kawahputih
        namaWisata.contains("Patenggang", ignoreCase = true) -> R.drawable.situpatenggang
        namaWisata.contains("Curug Putri", ignoreCase = true) -> R.drawable.curugputri
        namaWisata.contains("Papandayan", ignoreCase = true) -> R.drawable.papandayan
        namaWisata.contains("Waterland", ignoreCase = true) -> R.drawable.cirebonwaterland
        namaWisata.contains("Kebun Raya", ignoreCase = true) -> R.drawable.kebunraya
        namaWisata.contains("Waduk Darma", ignoreCase = true) -> R.drawable.situpatenggang
        else -> R.drawable.kebunraya
    }

    val formattedHarga = try {
        val nominal = hargaTiket.toDouble().toInt()
        "Rp " + java.text.NumberFormat.getNumberInstance(java.util.Locale("in", "ID")).format(nominal)
    } catch (e: Exception) {
        "Rp $hargaTiket"
    }

    val namaKota = wilayah?.namaKabupaten?.replace("Kabupaten ", "")?.replace("Kota ", "")
        ?: lokasi.substringAfterLast(", ").trim()

    val formattedRating = if (rataRating != null) {
        String.format(java.util.Locale.US, "(%.1f)", rataRating)
    } else {
        "(5.0)"
    }

    // Mengurai gambar (JsonElement) menjadi list gambar URL secara super aman
    val fullGambarUrls = mutableListOf<String>()
    if (gambar != null) {
        try {
            if (gambar.isJsonArray) {
                val jsonArray = gambar.asJsonArray
                for (i in 0 until jsonArray.size()) {
                    val imgStr = jsonArray.get(i).asString
                    if (!imgStr.isNullOrEmpty()) {
                        val url = if (imgStr.startsWith("http")) imgStr else "https://fragment-blog-eggbeater.ngrok-free.dev/storage/$imgStr"
                        fullGambarUrls.add(url)
                    }
                }
            } else if (gambar.isJsonPrimitive) {
                val imgStr = gambar.asString
                if (!imgStr.isNullOrEmpty()) {
                    if (imgStr.contains(",")) {
                        imgStr.split(",").forEach { part ->
                            val trimmed = part.trim()
                            if (trimmed.isNotEmpty()) {
                                val url = if (trimmed.startsWith("http")) trimmed else "https://fragment-blog-eggbeater.ngrok-free.dev/storage/$trimmed"
                                fullGambarUrls.add(url)
                            }
                        }
                    } else {
                        val url = if (imgStr.startsWith("http")) imgStr else "https://fragment-blog-eggbeater.ngrok-free.dev/storage/$imgStr"
                        fullGambarUrls.add(url)
                    }
                }
            }
        } catch (e: Exception) {
            // Abaikan jika terjadi kegagalan parsing element
        }
    }

    val mainGambarUrl = if (fullGambarUrls.isNotEmpty()) fullGambarUrls[0] else null

    return Wisata(
        nama = namaWisata,
        lokasi = lokasi,
        gambar = localDrawable,
        kota = namaKota,
        harga = formattedHarga,
        deskripsi = deskripsi,
        rating = formattedRating,
        gambarUrl = mainGambarUrl,
        gambarUrls = if (fullGambarUrls.isNotEmpty()) fullGambarUrls else null,
        kategori = kategori ?: "Lainnya"
    )
}