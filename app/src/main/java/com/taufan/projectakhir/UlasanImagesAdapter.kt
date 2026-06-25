package com.taufan.projectakhir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taufan.projectakhir.api.RetrofitClient
import com.taufan.projectakhir.databinding.ItemUlasanImageBinding

class UlasanImagesAdapter(private val images: List<String>) :
    RecyclerView.Adapter<UlasanImagesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemUlasanImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUlasanImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = images[position]
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
        val finalUrl = when {
            path.startsWith("http") || path.startsWith("content") || path.startsWith("file") -> path
            else -> "$baseUrl/storage/$path"
        }
        
        GlideHelper.loadImage(holder.binding.ivUlasanImage, finalUrl, R.drawable.hero)
    }

    override fun getItemCount(): Int = images.size
}
