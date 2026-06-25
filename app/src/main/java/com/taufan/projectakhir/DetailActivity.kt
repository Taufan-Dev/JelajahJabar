package com.taufan.projectakhir

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.taufan.projectakhir.api.RetrofitClient
import com.taufan.projectakhir.api.SessionManager
import com.taufan.projectakhir.api.WisataDetailResponse
import com.taufan.projectakhir.api.RekomendasiRequest
import com.taufan.projectakhir.api.SimpleResponse
import com.taufan.projectakhir.api.TicketHistoryResponse
import com.taufan.projectakhir.databinding.ActivityDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var inputImagesAdapter: UlasanImagesAdapter

    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris.take(3))
            updateSelectedImagesPreview()
        } else {
            Toast.makeText(this, "Tidak ada gambar yang dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSelectedImagesPreview() {
        val stringUris = selectedImageUris.map { it.toString() }
        inputImagesAdapter = UlasanImagesAdapter(stringUris)
        binding.rvInputGambarPreview.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvInputGambarPreview.adapter = inputImagesAdapter
        binding.rvInputGambarPreview.visibility = if (stringUris.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun uriToMultipartPart(uri: Uri, partName: String): MultipartBody.Part? {
        val contentResolver = contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = java.io.File.createTempFile("upload_review_", ".$extension", cacheDir)
            tempFile.deleteOnExit()
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            
            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengatur padding agar tidak tertutup sistem bar (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Menangkap data Parcelable dari Intent secara aman (mendukung API 33+)
        val dataWisata = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_WISATA", Wisata::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Wisata>("EXTRA_WISATA")
        }

        if (dataWisata != null) {
            binding.apply {
                // 2. Set data ke komponen UI (Menggunakan Slider jika terdapat lebih dari 1 gambar)
                val sliderImages = dataWisata.gambarUrls
                if (!sliderImages.isNullOrEmpty() && sliderImages.size > 1) {
                    ivDetailGambar.visibility = View.GONE
                    vpDetailGambar.visibility = View.VISIBLE
                    tvImageIndicator.visibility = View.VISIBLE
                    layoutDots.visibility = View.VISIBLE
                    
                    tvImageIndicator.text = "1/${sliderImages.size}"
                    
                    val sliderAdapter = DetailImageAdapter(sliderImages, dataWisata.gambar)
                    vpDetailGambar.adapter = sliderAdapter
                    
                    // Setup dots indicator secara dinamis
                    layoutDots.removeAllViews()
                    val dotsCount = sliderImages.size
                    val dots = arrayOfNulls<ImageView>(dotsCount)
                    for (i in 0 until dotsCount) {
                        dots[i] = ImageView(this@DetailActivity).apply {
                            setImageDrawable(
                                androidx.core.content.ContextCompat.getDrawable(
                                    this@DetailActivity,
                                    if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive
                                )
                            )
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(8, 0, 8, 0)
                            }
                            layoutParams = params
                        }
                        layoutDots.addView(dots[i])
                    }
                    
                    vpDetailGambar.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            tvImageIndicator.text = "${position + 1}/${sliderImages.size}"
                            for (i in 0 until dotsCount) {
                                dots[i]?.setImageDrawable(
                                    androidx.core.content.ContextCompat.getDrawable(
                                        this@DetailActivity,
                                        if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
                                    )
                                )
                            }
                        }
                    })
                } else {
                    vpDetailGambar.visibility = View.GONE
                    tvImageIndicator.visibility = View.GONE
                    layoutDots.visibility = View.GONE
                    ivDetailGambar.visibility = View.VISIBLE
                    
                    GlideHelper.loadImage(ivDetailGambar, dataWisata.gambarUrl, dataWisata.gambar)
                }

                tvDetailJudul.text = dataWisata.nama
                tvDetailLokasi.text = "📍 ${dataWisata.lokasi}"
                tvDetailHarga.text = dataWisata.harga
                tvDetailDeskripsi.text = dataWisata.deskripsi
                tvDetailRating.text = dataWisata.rating

                // Panggil ulasan dan periksa kelayakan menulis ulasan
                loadUlasanAndCheckEligibility(dataWisata.id)

                // 3. Logika Favorite (Cek status dari manager)
                cbFavorite.isChecked = FavoriteManager.isFavorite(dataWisata)

                cbFavorite.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        FavoriteManager.addFavorite(dataWisata)
                        Toast.makeText(this@DetailActivity, "Simpan ke Favorit ❤️", Toast.LENGTH_SHORT).show()
                    } else {
                        FavoriteManager.removeFavorite(dataWisata)
                        Toast.makeText(this@DetailActivity, "Dihapus dari Favorit", Toast.LENGTH_SHORT).show()
                    }
                }

                // 4. Logika Tombol Beli Tiket (Pindah ke Konfirmasi Pembayaran)
                btnBeliTiket.setOnClickListener {
                    val intentBayar = Intent(this@DetailActivity, KonfirmasiPembayaranActivity::class.java)

                    // Mengirimkan data yang dibutuhkan ke halaman pembayaran
                    intentBayar.putExtra("WISATA_ID", dataWisata.id)
                    intentBayar.putExtra("NAMA_WISATA", dataWisata.nama)
                    intentBayar.putExtra("HARGA_WISATA", dataWisata.harga)
                    intentBayar.putExtra("LOKASI_WISATA", dataWisata.lokasi)
                    intentBayar.putExtra("GAMBAR_WISATA", dataWisata.gambar)
                    intentBayar.putExtra("GAMBAR_URL_WISATA", dataWisata.gambarUrl)

                    startActivity(intentBayar)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }

                // 5. Tombol Back
                ivBack.setOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }
    }

    private fun loadUlasanAndCheckEligibility(wisataId: Int) {
        // 1. Ambil detail ulasan dari API
        RetrofitClient.instance.getWisataDetail(wisataId).enqueue(object : Callback<WisataDetailResponse> {
            override fun onResponse(call: Call<WisataDetailResponse>, response: Response<WisataDetailResponse>) {
                if (response.isSuccessful) {
                    val wisataItem = response.body()?.data
                    val ulasans = wisataItem?.rekomendasis ?: emptyList()
                    
                    // Update rata-rata rating secara dinamis jika ada data ter-update
                    if (wisataItem?.rataRating != null) {
                        binding.tvDetailRating.text = String.format(java.util.Locale.US, "(%.1f)", wisataItem.rataRating)
                    }

                    if (ulasans.isEmpty()) {
                        binding.tvNoUlasan.visibility = View.VISIBLE
                        binding.rvUlasan.visibility = View.GONE
                    } else {
                        binding.tvNoUlasan.visibility = View.GONE
                        binding.rvUlasan.visibility = View.VISIBLE
                        binding.rvUlasan.layoutManager = LinearLayoutManager(this@DetailActivity)
                        binding.rvUlasan.adapter = UlasanAdapter(ulasans)
                    }

                    // Pengecekan apakah user sudah memenuhi syarat untuk menulis ulasan
                    val sessionManager = SessionManager(this@DetailActivity)
                    val token = sessionManager.getToken()
                    val userEmail = sessionManager.getUserEmail() ?: ""
                    
                    if (!token.isNullOrEmpty()) {
                        val totalReviewsSubmitted = ulasans.count { it.user?.email == userEmail }
                        checkTicketHistoryForReview(token, wisataId, totalReviewsSubmitted)
                    } else {
                        binding.cvTulisUlasan.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<WisataDetailResponse>, t: Throwable) {
                // Tampilkan ulasan lokal kosong jika API gagal
                binding.tvNoUlasan.visibility = View.VISIBLE
                binding.rvUlasan.visibility = View.GONE
            }
        })
    }

    private fun checkTicketHistoryForReview(token: String, wisataId: Int, totalReviewsSubmitted: Int) {
        RetrofitClient.instance.getTicketHistory("Bearer $token").enqueue(object : Callback<TicketHistoryResponse> {
            override fun onResponse(call: Call<TicketHistoryResponse>, response: Response<TicketHistoryResponse>) {
                if (response.isSuccessful) {
                    val tickets = response.body()?.data ?: emptyList()
                    // Hitung total tiket berstatus 'paid' untuk wisata ini
                    val totalPaidTickets = tickets.count { it.wisataId == wisataId && it.status == "paid" }
                    if (totalPaidTickets > totalReviewsSubmitted) {
                        binding.cvTulisUlasan.visibility = View.VISIBLE
                        // Reset state form ulasan agar bersih
                        selectedImageUris.clear()
                        updateSelectedImagesPreview()
                        setupTulisUlasanListener(token, wisataId)
                    } else {
                        binding.cvTulisUlasan.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<TicketHistoryResponse>, t: Throwable) {
                binding.cvTulisUlasan.visibility = View.GONE
            }
        })
    }

    private fun setupTulisUlasanListener(token: String, wisataId: Int) {
        binding.btnPilihGambar.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnKirimUlasan.setOnClickListener {
            val rating = binding.rbInputStars.rating.toInt()
            val comment = binding.etInputUlasan.text.toString().trim()

            if (comment.isEmpty()) {
                Toast.makeText(this, "Silakan isi komentar ulasan Anda terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnKirimUlasan.isEnabled = false
            binding.btnKirimUlasan.text = "Mengirim..."

            val wisataIdPart = wisataId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val ratingPart = rating.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val ulasanPart = comment.toRequestBody("text/plain".toMediaTypeOrNull())

            // Konversi Uri gambar pilihan ke MultipartBody.Part
            val imageParts = mutableListOf<MultipartBody.Part>()
            for (uri in selectedImageUris) {
                val part = uriToMultipartPart(uri, "gambar[]")
                if (part != null) {
                    imageParts.add(part)
                }
            }

            RetrofitClient.instance.submitReviewMultipart(
                "Bearer $token",
                wisataIdPart,
                ratingPart,
                ulasanPart,
                if (imageParts.isNotEmpty()) imageParts else null
            ).enqueue(object : Callback<SimpleResponse> {
                override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                    binding.btnKirimUlasan.isEnabled = true
                    binding.btnKirimUlasan.text = "Kirim Ulasan"
                    if (response.isSuccessful) {
                        Toast.makeText(this@DetailActivity, "Ulasan berhasil dikirim, terima kasih!", Toast.LENGTH_SHORT).show()
                        binding.etInputUlasan.setText("")
                        binding.cvTulisUlasan.visibility = View.GONE
                        selectedImageUris.clear()
                        updateSelectedImagesPreview()
                        // Refresh ulasan
                        loadUlasanAndCheckEligibility(wisataId)
                    } else {
                        val errorMsg = response.message() ?: "Gagal mengirim ulasan"
                        Toast.makeText(this@DetailActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    binding.btnKirimUlasan.isEnabled = true
                    binding.btnKirimUlasan.text = "Kirim Ulasan"
                    Toast.makeText(this@DetailActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}

// Adapter khusus Slider untuk ViewPager2 di DetailActivity
class DetailImageAdapter(
    private val images: List<String>,
    private val placeholderDrawable: Int
) : RecyclerView.Adapter<DetailImageAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_slider, parent, false) as ImageView
        return SliderViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val imageUrl = images[position]
        GlideHelper.loadImage(holder.imageView, imageUrl, placeholderDrawable)
    }

    override fun getItemCount(): Int = images.size
}