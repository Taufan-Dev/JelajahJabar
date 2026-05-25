package com.taufan.projectakhir

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taufan.projectakhir.databinding.ItemWisataHorizontalBinding

class PopulerAdapter(private val listPopuler: List<Wisata>) :
    RecyclerView.Adapter<PopulerAdapter.PopulerViewHolder>() {

    inner class PopulerViewHolder(val binding: ItemWisataHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopulerViewHolder {
        val binding = ItemWisataHorizontalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PopulerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopulerViewHolder, position: Int) {
        val data = listPopuler[position]
        holder.binding.apply {
            tvNamaPopuler.text = data.nama
            tvLokasiPopuler.text = "📍 ${data.lokasi}"
            
            if (!data.gambarUrl.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(ivPopuler.context)
                    .load(data.gambarUrl)
                    .placeholder(data.gambar)
                    .error(data.gambar)
                    .into(ivPopuler)
            } else {
                ivPopuler.setImageResource(data.gambar)
            }

            // Klik kartu untuk ke Detail
            root.setOnClickListener {
                val intent = Intent(root.context, DetailActivity::class.java)
                intent.putExtra("EXTRA_WISATA", data)
                root.context.startActivity(intent)
                (root.context as? android.app.Activity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    override fun getItemCount(): Int = listPopuler.size
}