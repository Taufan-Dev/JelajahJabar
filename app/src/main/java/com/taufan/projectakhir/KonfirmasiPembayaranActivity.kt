package com.taufan.projectakhir

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.taufan.projectakhir.databinding.ActivityKonfirmasiPembayaranBinding
import java.util.Calendar

class KonfirmasiPembayaranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKonfirmasiPembayaranBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKonfirmasiPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Ambil data dari Intent (Data dikirim dari DetailActivity)
        val nama = intent.getStringExtra("NAMA_WISATA") ?: "Nama Wisata"
        val hargaString = intent.getStringExtra("HARGA_WISATA") ?: "Rp 0"
        val gambar = intent.getIntExtra("GAMBAR_WISATA", 0)
        val gambarUrl = intent.getStringExtra("GAMBAR_URL_WISATA")
        val lokasi = intent.getStringExtra("LOKASI_WISATA") ?: ""

        // 2. Set Data ke UI
        binding.apply {
            // Data Wisata (pada bagian <include>)
            cardWisata.tvNamaWisataFav.text = nama
            cardWisata.tvLokasiFav.text = "📍 $lokasi"
            
            // Load Gambar menggunakan Glide jika URL tersedia
            if (!gambarUrl.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(cardWisata.ivWisataFav.context)
                    .load(gambarUrl)
                    .placeholder(gambar)
                    .error(gambar)
                    .into(cardWisata.ivWisataFav)
            } else {
                cardWisata.ivWisataFav.setImageResource(gambar)
            }

            // Data Ringkasan
            tvHargaTiket.text = hargaString

            // Logika sederhana menghitung total (Asumsi biaya layanan 4000)
            val hargaBersih = hargaString.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
            val total = hargaBersih + 4000
            tvTotalBayar.text = "Rp $total"
        }

        // 3. Logika Pilih Tanggal (DatePicker)
        binding.btnPilihTanggal.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.tvTanggalTerpilih.text = dateStr
            }, year, month, day)

            dpd.show()
        }

        // 4. Logika Tombol Bayar
        binding.btnBayarTiket.setOnClickListener {
            val tanggal = binding.tvTanggalTerpilih.text.toString()

            if (tanggal == "Pilih tanggal") {
                Toast.makeText(this, "Silakan pilih tanggal kunjungan dulu!", Toast.LENGTH_SHORT).show()
            } else {
                // Skenario jika berhasil
                Toast.makeText(this, "Pembayaran Berhasil! Tiket dikirim ke Email.", Toast.LENGTH_LONG).show()
                finish() // Menutup halaman dan kembali
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        // 5. Tombol Kembali
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}