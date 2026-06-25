package com.taufan.projectakhir

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taufan.projectakhir.databinding.ItemExploreBinding

class ExploreAdapter(private var listWisata: List<Wisata>) :
    RecyclerView.Adapter<ExploreAdapter.WisataViewHolder>() {

    inner class WisataViewHolder(val binding: ItemExploreBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val binding = ItemExploreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WisataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        val data = listWisata[position]
        holder.binding.apply {
            tvNamaWisata.text = data.nama
            tvLokasi.text = "📍 ${data.kota}"
            tvHarga.text = data.harga
            tvRating.text = data.rating.replace("(", "").replace(")", "")
            
            GlideHelper.loadImage(ivWisata, data.gambarUrl, data.gambar)

            root.setOnClickListener {
                val intent = Intent(root.context, DetailActivity::class.java)
                intent.putExtra("EXTRA_WISATA", data)
                root.context.startActivity(intent)
                (root.context as? android.app.Activity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    override fun getItemCount(): Int = listWisata.size

    fun updateData(newList: List<Wisata>) {
        listWisata = newList
        notifyDataSetChanged()
    }
}