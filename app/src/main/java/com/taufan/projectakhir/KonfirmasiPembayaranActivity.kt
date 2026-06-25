package com.taufan.projectakhir

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taufan.projectakhir.api.RetrofitClient
import com.taufan.projectakhir.api.SessionManager
import com.taufan.projectakhir.api.TicketRequest
import com.taufan.projectakhir.api.TicketResponse
import com.taufan.projectakhir.databinding.ActivityKonfirmasiPembayaranBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class KonfirmasiPembayaranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKonfirmasiPembayaranBinding
    private lateinit var sessionManager: SessionManager
    private var wisataId: Int = 0
    private var hargaBersih: Int = 0
    private var jumlahTiket: Int = 1
    private var selectedDateFormatted: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKonfirmasiPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        // 1. Ambil data dari Intent
        wisataId = intent.getIntExtra("WISATA_ID", 0)
        val nama = intent.getStringExtra("NAMA_WISATA") ?: "Nama Wisata"
        val hargaString = intent.getStringExtra("HARGA_WISATA") ?: "Rp 0"
        val gambar = intent.getIntExtra("GAMBAR_WISATA", 0)
        val gambarUrl = intent.getStringExtra("GAMBAR_URL_WISATA")
        val lokasi = intent.getStringExtra("LOKASI_WISATA") ?: ""

        hargaBersih = hargaString.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

        // 2. Set Data ke UI awal
        binding.apply {
            cardWisata.tvNamaWisataFav.text = nama
            cardWisata.tvLokasiFav.text = "📍 $lokasi"
            
            GlideHelper.loadImage(cardWisata.ivWisataFav, gambarUrl, gambar)

            tvHargaTiket.text = hargaString
            updateTotalBayar()
        }

        // 3. Counter Tambah/Kurang Tiket
        binding.btnPlus.setOnClickListener {
            jumlahTiket++
            updateTotalBayar()
        }

        binding.btnMinus.setOnClickListener {
            if (jumlahTiket > 1) {
                jumlahTiket--
                updateTotalBayar()
            }
        }

        // 4. Logika Pilih Tanggal (DatePicker)
        binding.btnPilihTanggal.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateShow = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.tvTanggalTerpilih.text = dateShow
                
                // Format YYYY-MM-DD untuk API Laravel
                selectedDateFormatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            }, year, month, day)

            // Batasi pilihan tanggal agar tidak bisa memilih tanggal yang sudah lewat
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000

            dpd.show()
        }

        // 5. Logika Tombol Bayar Tiket (Booking via API)
        binding.btnBayarTiket.setOnClickListener {
            if (selectedDateFormatted.isEmpty()) {
                Toast.makeText(this, "Silakan pilih tanggal kunjungan dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            bookTicket()
        }

        // 6. Tombol Kembali
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun updateTotalBayar() {
        binding.tvJumlahTiket.text = jumlahTiket.toString()
        val total = hargaBersih * jumlahTiket
        
        val format = java.text.NumberFormat.getNumberInstance(java.util.Locale("in", "ID"))
        binding.tvTotalBayar.text = "Rp ${format.format(total)}"
    }

    private fun bookTicket() {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show()
            return
        }

        setLoadingState(true)

        val request = TicketRequest(wisataId, jumlahTiket, selectedDateFormatted)
        RetrofitClient.instance.bookTicket("Bearer $token", request).enqueue(object : Callback<TicketResponse> {
            override fun onResponse(call: Call<TicketResponse>, response: Response<TicketResponse>) {
                setLoadingState(false)

                if (response.isSuccessful) {
                    val ticketResponse = response.body()
                    if (ticketResponse?.status == "success" && ticketResponse.data != null) {
                        val ticketData = ticketResponse.data
                        val snapToken = ticketData.snapToken ?: ""
                        val kodeTiket = ticketData.kodeTiket ?: ""

                        if (snapToken.isNotEmpty()) {
                            // Pindah ke PaymentActivity untuk pembayaran Webview
                            val intentPayment = Intent(this@KonfirmasiPembayaranActivity, PaymentActivity::class.java).apply {
                                putExtra("SNAP_TOKEN", snapToken)
                                putExtra("KODE_TIKET", kodeTiket)
                            }
                            startActivity(intentPayment)
                            finish()
                        } else {
                            Toast.makeText(this@KonfirmasiPembayaranActivity, "Gagal memproses token Midtrans", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val msg = ticketResponse?.message ?: "Gagal memesan tiket, periksa kembali"
                        Toast.makeText(this@KonfirmasiPembayaranActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@KonfirmasiPembayaranActivity, "Terjadi kesalahan (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TicketResponse>, t: Throwable) {
                setLoadingState(false)
                Toast.makeText(this@KonfirmasiPembayaranActivity, "Tidak dapat menghubungi server: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.btnBayarTiket.isEnabled = false
            binding.btnBayarTiket.text = "Memproses pemesanan..."
            binding.btnPlus.isEnabled = false
            binding.btnMinus.isEnabled = false
            binding.btnPilihTanggal.isEnabled = false
        } else {
            binding.btnBayarTiket.isEnabled = true
            binding.btnBayarTiket.text = "Bayar Tiket Sekarang"
            binding.btnPlus.isEnabled = true
            binding.btnMinus.isEnabled = true
            binding.btnPilihTanggal.isEnabled = true
        }
    }
}