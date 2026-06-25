package com.taufan.projectakhir

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taufan.projectakhir.api.RetrofitClient
import com.taufan.projectakhir.api.SimulateCallbackRequest
import com.taufan.projectakhir.api.SimulateCallbackResponse
import com.taufan.projectakhir.databinding.ActivityPaymentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var snapToken: String = ""
    private var kodeTiket: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        snapToken = intent.getStringExtra("SNAP_TOKEN") ?: ""
        kodeTiket = intent.getStringExtra("KODE_TIKET") ?: ""

        if (snapToken.isEmpty()) {
            Toast.makeText(this, "Token pembayaran tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupWebView()

        binding.btnClosePayment.setOnClickListener {
            finish()
        }

        // Fitur simulator: mengirimkan simulasi status lunas/settlement ke backend
        binding.btnSimulateSuccess.setOnClickListener {
            simulatePaymentSuccess()
        }
    }

    private fun setupWebView() {
        val webSettings = binding.wvPayment.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true

        binding.wvPayment.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.pbPayment.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }

        binding.wvPayment.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    binding.pbPayment.visibility = View.GONE
                } else {
                    binding.pbPayment.visibility = View.VISIBLE
                }
            }
        }

        // Load Midtrans Sandbox snap page menggunakan snapToken
        val snapUrl = "https://app.sandbox.midtrans.com/snap/v2/vtweb/$snapToken"
        binding.wvPayment.loadUrl(snapUrl)
    }

    private fun simulatePaymentSuccess() {
        if (kodeTiket.isEmpty()) {
            Toast.makeText(this, "Kode tiket tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSimulateSuccess.isEnabled = false
        binding.btnSimulateSuccess.text = "Mengirim simulasi..."

        val request = SimulateCallbackRequest(kodeTiket, "settlement")
        RetrofitClient.instance.simulateCallback(request).enqueue(object : Callback<SimulateCallbackResponse> {
            override fun onResponse(
                call: Call<SimulateCallbackResponse>,
                response: Response<SimulateCallbackResponse>
            ) {
                binding.btnSimulateSuccess.isEnabled = true
                binding.btnSimulateSuccess.text = "Simulasikan Bayar Lunas"

                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@PaymentActivity, "Simulasi Sukses! Pembayaran Terkonfirmasi Lunas 🎉", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val msg = response.body()?.message ?: "Gagal mengirim simulasi"
                    Toast.makeText(this@PaymentActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SimulateCallbackResponse>, t: Throwable) {
                binding.btnSimulateSuccess.isEnabled = true
                binding.btnSimulateSuccess.text = "Simulasikan Bayar Lunas"
                Toast.makeText(this@PaymentActivity, "Kesalahan koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
