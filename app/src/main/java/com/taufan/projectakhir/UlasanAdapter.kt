package com.taufan.projectakhir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taufan.projectakhir.api.RekomendasiData
import com.taufan.projectakhir.databinding.ItemUlasanBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.google.gson.JsonElement
import com.google.gson.JsonParser

class UlasanAdapter(private val list: List<RekomendasiData>) :
    RecyclerView.Adapter<UlasanAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemUlasanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUlasanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvUlasanUser.text = item.user?.name ?: "Pengguna"
            tvUlasanText.text = item.ulasan ?: "-"
            rbUlasanStars.rating = item.rating.toFloat()

            // Format date from "2026-06-24T15:00:00.000000Z" to "24 Jun 2026"
            val rawDate = item.createdAt
            if (!rawDate.isNullOrEmpty()) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val date = inputFormat.parse(rawDate)
                    if (date != null) {
                        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
                        tvUlasanDate.text = outputFormat.format(date)
                    } else {
                        tvUlasanDate.text = rawDate.substringBefore("T")
                    }
                } catch (e: Exception) {
                    tvUlasanDate.text = rawDate.substringBefore("T")
                }
            } else {
                tvUlasanDate.text = "-"
            }

            // Render review images
            val gambarList = parseGambarList(item.gambar)
            if (gambarList.isNotEmpty()) {
                rvUlasanImages.visibility = View.VISIBLE
                rvUlasanImages.layoutManager = LinearLayoutManager(
                    holder.itemView.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                rvUlasanImages.adapter = UlasanImagesAdapter(gambarList)
            } else {
                rvUlasanImages.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = list.size

    private fun parseGambarList(gambarElement: JsonElement?): List<String> {
        if (gambarElement == null || gambarElement.isJsonNull) return emptyList()
        
        val list = mutableListOf<String>()
        if (gambarElement.isJsonArray) {
            val jsonArray = gambarElement.asJsonArray
            for (i in 0 until jsonArray.size()) {
                val elem = jsonArray.get(i)
                if (elem.isJsonPrimitive && elem.asJsonPrimitive.isString) {
                    list.add(elem.asString)
                }
            }
        } else if (gambarElement.isJsonPrimitive && gambarElement.asJsonPrimitive.isString) {
            val str = gambarElement.asString
            if (str.startsWith("[") && str.endsWith("]")) {
                try {
                    val parsedArray = JsonParser.parseString(str)
                    if (parsedArray.isJsonArray) {
                        val jsonArray = parsedArray.asJsonArray
                        for (i in 0 until jsonArray.size()) {
                            val elem = jsonArray.get(i)
                            if (elem.isJsonPrimitive && elem.asJsonPrimitive.isString) {
                                list.add(elem.asString)
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (str.isNotBlank()) list.add(str)
                }
            } else {
                if (str.isNotBlank()) {
                    list.add(str)
                }
            }
        }
        return list
    }
}
