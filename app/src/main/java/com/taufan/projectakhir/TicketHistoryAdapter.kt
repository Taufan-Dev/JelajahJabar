package com.taufan.projectakhir

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.taufan.projectakhir.api.TicketHistoryItem
import com.taufan.projectakhir.databinding.ItemTicketHistoryBinding
import java.text.NumberFormat
import java.util.Locale

class TicketHistoryAdapter(
    private val context: Context,
    private val listTicket: List<TicketHistoryItem>,
    private val onQrClickListener: (String) -> Unit
) : RecyclerView.Adapter<TicketHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemTicketHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemTicketHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val data = listTicket[position]
        holder.binding.apply {
            // Set Data Utama
            tvWisataName.text = data.wisata?.namaWisata ?: "Wisata"
            tvKodeTiket.text = data.kodeTiket ?: "N/A"
            tvTanggalKunjungan.text = data.tanggalKunjungan ?: "N/A"
            tvJumlahTiket.text = "${data.jumlahTiket ?: 0} Tiket"

            val format = NumberFormat.getNumberInstance(Locale("in", "ID"))
            val total = data.totalBayar ?: 0.0
            tvTotalBayar.text = "Rp ${format.format(total)}"

            // Set Status & Badge Color
            val isUsed = data.statusTiket?.lowercase() == "used"

            if (isUsed) {
                tvStatus.text = "USED"
                cvStatus.setCardBackgroundColor(Color.parseColor("#71717A")) // Zinc Gray
                btnAction.visibility = View.VISIBLE
                btnAction.text = "Tiket Sudah Digunakan"
                btnAction.isEnabled = false
                btnAction.setBackgroundColor(Color.parseColor("#A1A1AA")) // Light Gray
            } else {
                btnAction.isEnabled = true
                val status = (data.status ?: "pending").lowercase()
                tvStatus.text = status.uppercase()

                when (status) {
                    "success", "settlement", "paid" -> {
                        cvStatus.setCardBackgroundColor(Color.parseColor("#1BBA85")) // Hijau Lunas
                        btnAction.visibility = View.VISIBLE
                        btnAction.text = "Tampilkan QR Code 🔍"
                        btnAction.setBackgroundColor(Color.parseColor("#1BBA85"))
                        
                        btnAction.setOnClickListener {
                            onQrClickListener(data.kodeTiket ?: "")
                        }
                    }
                    "pending" -> {
                        cvStatus.setCardBackgroundColor(Color.parseColor("#FF9800")) // Kuning Pending
                        btnAction.visibility = View.VISIBLE
                        btnAction.text = "Bayar Sekarang 💳"
                        btnAction.setBackgroundColor(Color.parseColor("#FF9800"))
                        
                        btnAction.setOnClickListener {
                            val intent = Intent(context, PaymentActivity::class.java).apply {
                                putExtra("SNAP_TOKEN", data.snapToken ?: "")
                                putExtra("KODE_TIKET", data.kodeTiket ?: "")
                            }
                            context.startActivity(intent)
                        }
                    }
                    else -> {
                        cvStatus.setCardBackgroundColor(Color.parseColor("#EF4444")) // Merah Gagal
                        btnAction.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = listTicket.size

}
