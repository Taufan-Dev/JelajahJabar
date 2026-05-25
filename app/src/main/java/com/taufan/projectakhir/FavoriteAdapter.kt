package com.taufan.projectakhir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taufan.projectakhir.databinding.ItemWisataFavoriteBinding

class FavoriteAdapter(private val listFav: List<Wisata>) :
    RecyclerView.Adapter<FavoriteAdapter.FavViewHolder>() {

    inner class FavViewHolder(val binding: ItemWisataFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val binding = ItemWisataFavoriteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val data = listFav[position]
        holder.binding.apply {
            tvNamaWisataFav.text = data.nama
            tvLokasiFav.text = "📍 ${data.lokasi}"
            
            if (!data.gambarUrl.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(ivWisataFav.context)
                    .load(data.gambarUrl)
                    .placeholder(data.gambar)
                    .error(data.gambar)
                    .into(ivWisataFav)
            } else {
                ivWisataFav.setImageResource(data.gambar)
            }
            // Ikon love di sini sudah otomatis merah sesuai XML
        }
    }

    override fun getItemCount(): Int = listFav.size
}