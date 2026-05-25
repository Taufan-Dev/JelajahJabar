package com.taufan.projectakhir

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.taufan.projectakhir.databinding.ItemExploreBinding
import com.taufan.projectakhir.databinding.ItemWisataHorizontalBinding

class ExploreAdapter(private var listWisata: List<Wisata>) :
    RecyclerView.Adapter<ExploreAdapter.WisataViewHolder>() {

    // Menggunakan ViewBinding umum agar bisa fleksibel menggunakan dua layout berbeda jika perlu
    inner class WisataViewHolder(val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        // Cek parent: Jika orientasi RecyclerView-nya horizontal, pakai layout horizontal
        // Namun cara paling aman adalah membuat adapter ini mengenali layout mana yang dipanggil.
        // Untuk saat ini, kita gunakan ItemExploreBinding sebagai default.
        val binding = ItemExploreBinding.inflate(inflater, parent, false)
        return WisataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        val data = listWisata[position]
        val binding = holder.binding as ItemExploreBinding

        binding.apply {
            // 1. Set Data ke UI
            tvNamaWisata.text = data.nama
            tvLokasi.text = "📍 ${data.lokasi}"
            
            if (!data.gambarUrl.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(ivWisata.context)
                    .load(data.gambarUrl)
                    .placeholder(data.gambar)
                    .error(data.gambar)
                    .into(ivWisata)
            } else {
                ivWisata.setImageResource(data.gambar)
            }

            // 2. Klik Kartu -> Pindah ke DetailActivity
            root.setOnClickListener {
                val intent = Intent(root.context, DetailActivity::class.java)
                intent.putExtra("EXTRA_WISATA", data)
                root.context.startActivity(intent)
                (root.context as? android.app.Activity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    override fun getItemCount(): Int = listWisata.size

    // Fungsi sakti untuk update data saat TabCity diklik
    fun updateData(newList: List<Wisata>) {
        listWisata = newList
        notifyDataSetChanged()
    }
}