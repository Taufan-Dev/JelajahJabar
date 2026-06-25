package com.taufan.projectakhir

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import android.os.Handler
import android.os.Looper
import com.taufan.projectakhir.api.RetrofitClient
import com.taufan.projectakhir.api.SessionManager
import com.taufan.projectakhir.api.TicketHistoryResponse
import com.taufan.projectakhir.databinding.ActivityTicketHistoryBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TicketHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketHistoryBinding
    private lateinit var sessionManager: SessionManager

    // Polling properties for real-time ticket scan detection
    private var activeQrKodeTiket: String? = null
    private var activeDialog: AlertDialog? = null
    private val pollingHandler = Handler(Looper.getMainLooper())
    private val pollingRunnable = object : Runnable {
        override fun run() {
            if (activeQrKodeTiket != null) {
                loadTicketHistory(isPolling = true)
                pollingHandler.postDelayed(this, 3000) // Poll every 3 seconds
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.rvTicketHistory.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        // Mengambil riwayat tiket di onResume agar status langsung ter-update setelah bayar / simulasi
        loadTicketHistory(isPolling = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }

    private fun loadTicketHistory(isPolling: Boolean = false) {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            if (!isPolling) {
                binding.pbHistory.visibility = View.GONE
                Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (!isPolling) {
            binding.pbHistory.visibility = View.VISIBLE
            binding.layoutEmptyHistory.visibility = View.GONE
            binding.rvTicketHistory.visibility = View.GONE
        }

        RetrofitClient.instance.getTicketHistory("Bearer $token").enqueue(object : Callback<TicketHistoryResponse> {
            override fun onResponse(
                call: Call<TicketHistoryResponse>,
                response: Response<TicketHistoryResponse>
            ) {
                if (!isPolling) {
                    binding.pbHistory.visibility = View.GONE
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        val ticketList = body.data ?: emptyList()
                        if (ticketList.isNotEmpty()) {
                            binding.rvTicketHistory.visibility = View.VISIBLE
                            binding.layoutEmptyHistory.visibility = View.GONE
                            
                            val adapter = TicketHistoryAdapter(this@TicketHistoryActivity, ticketList) { kodeTiket ->
                                showQrCodeDialog(kodeTiket)
                            }
                            binding.rvTicketHistory.adapter = adapter

                            // Cek jika tiket yang sedang ditampilkan QR-nya sudah di-scan (status_tiket == "used")
                            activeQrKodeTiket?.let { activeKode ->
                                val activeTicket = ticketList.find { it.kodeTiket == activeKode }
                                if (activeTicket != null && activeTicket.statusTiket == "used") {
                                    showValidationSuccessDialog(activeKode, activeTicket.wisata?.namaWisata ?: "Tempat Wisata")
                                }
                            }
                        } else {
                            showEmptyState()
                        }
                    } else {
                        if (!isPolling) {
                            showEmptyState()
                            Toast.makeText(this@TicketHistoryActivity, body?.message ?: "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    if (!isPolling) {
                        showEmptyState()
                        Toast.makeText(this@TicketHistoryActivity, "Kesalahan server (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<TicketHistoryResponse>, t: Throwable) {
                if (!isPolling) {
                    binding.pbHistory.visibility = View.GONE
                    showEmptyState()
                    Toast.makeText(this@TicketHistoryActivity, "Gagal menghubungkan internet: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showQrCodeDialog(kodeTiket: String) {
        val dialogBinding = com.taufan.projectakhir.databinding.DialogQrCodeBinding.inflate(
            LayoutInflater.from(this)
        )

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogBinding.tvDialogKodeTiket.text = kodeTiket

        // Fetch SVG from backend
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("${com.taufan.projectakhir.api.RetrofitClient.BASE_URL.removeSuffix("/")}/api/tiket/$kodeTiket/qrcode")
            .header("ngrok-skip-browser-warning", "true")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                runOnUiThread {
                    dialogBinding.pbQrLoading.visibility = View.GONE
                    Toast.makeText(this@TicketHistoryActivity, "Gagal memuat QR Code", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val svgContent = response.body?.string() ?: ""
                runOnUiThread {
                    dialogBinding.pbQrLoading.visibility = View.GONE

                    val htmlContent = """
                        <html>
                        <head>
                            <style>
                                body {
                                    margin: 0;
                                    padding: 0;
                                    display: flex;
                                    justify-content: center;
                                    align-items: center;
                                    height: 100vh;
                                    background-color: transparent;
                                }
                                svg {
                                    width: 100% !important;
                                    height: 100% !important;
                                    max-width: 180px;
                                    max-height: 180px;
                                }
                            </style>
                        </head>
                        <body>
                            $svgContent
                        </body>
                        </html>
                    """.trimIndent()

                    dialogBinding.wvQrCode.settings.javaScriptEnabled = true
                    dialogBinding.wvQrCode.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            }
        })

        dialogBinding.btnDialogClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            stopPolling()
        }

        activeQrKodeTiket = kodeTiket
        activeDialog = dialog
        dialog.show()

        startPolling()
    }

    private fun startPolling() {
        pollingHandler.removeCallbacks(pollingRunnable)
        pollingHandler.postDelayed(pollingRunnable, 3000)
    }

    private fun stopPolling() {
        activeQrKodeTiket = null
        activeDialog = null
        pollingHandler.removeCallbacks(pollingRunnable)
    }

    private fun showValidationSuccessDialog(kodeTiket: String, namaWisata: String) {
        activeDialog?.dismiss()
        stopPolling()

        AlertDialog.Builder(this)
            .setTitle("Tiket Berhasil Discan! 🎉")
            .setMessage("Tiket dengan kode $kodeTiket telah berhasil divalidasi oleh petugas loket.\n\nSelamat masuk dan selamat menikmati waktu liburan Anda di $namaWisata! 🌳✨")
            .setPositiveButton("Terima Kasih") { d, _ ->
                d.dismiss()
                loadTicketHistory(isPolling = false)
            }
            .setCancelable(false)
            .show()
    }

    private fun showEmptyState() {
        binding.rvTicketHistory.visibility = View.GONE
        binding.layoutEmptyHistory.visibility = View.VISIBLE
    }
}
