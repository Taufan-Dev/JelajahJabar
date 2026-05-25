package com.taufan.projectakhir

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.taufan.projectakhir.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

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
                    
                    tvImageIndicator.text = "1/${sliderImages.size}"
                    
                    val sliderAdapter = DetailImageAdapter(sliderImages, dataWisata.gambar)
                    vpDetailGambar.adapter = sliderAdapter
                    
                    vpDetailGambar.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            tvImageIndicator.text = "${position + 1}/${sliderImages.size}"
                        }
                    })
                } else {
                    vpDetailGambar.visibility = View.GONE
                    tvImageIndicator.visibility = View.GONE
                    ivDetailGambar.visibility = View.VISIBLE
                    
                    if (!dataWisata.gambarUrl.isNullOrEmpty()) {
                        Glide.with(ivDetailGambar.context)
                            .load(dataWisata.gambarUrl)
                            .placeholder(dataWisata.gambar)
                            .error(dataWisata.gambar)
                            .into(ivDetailGambar)
                    } else {
                        ivDetailGambar.setImageResource(dataWisata.gambar)
                    }
                }

                tvDetailJudul.text = dataWisata.nama
                tvDetailLokasi.text = "📍 ${dataWisata.lokasi}"
                tvDetailHarga.text = dataWisata.harga
                tvDetailDeskripsi.text = dataWisata.deskripsi
                tvDetailRating.text = dataWisata.rating

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
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .placeholder(placeholderDrawable)
            .error(placeholderDrawable)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size
}