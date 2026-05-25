package com.taufan.projectakhir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.taufan.projectakhir.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 1. Inisialisasi daftar wisata kosong (akan dimuat dinamis dari API)
    private var listWisataPopuler = emptyList<Wisata>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Jalankan setup RecyclerView Populer dengan data fallback lokal terlebih dahulu
        setupPopulerRecyclerView(listWisataPopuler)

        // Ambil data terbaru dari API ngrok
        fetchWisataFromApi()

        // 3. Logika klik "Lihat Semua" pindah ke Explore
        binding.tvLihatSemua.setOnClickListener {
            (activity as? MainActivity)?.moveToExplore()
        }

        // 4. Logika klik kategori untuk filter popular list secara interaktif
        var activeCategory: String? = null

        fun applyCategoryFilter(category: String) {
            // Reset background semua card category
            binding.apply {
                cvCategoryGunung.setCardBackgroundColor(android.graphics.Color.parseColor("#F8FDFB"))
                cvCategoryPantai.setCardBackgroundColor(android.graphics.Color.parseColor("#F8FDFB"))
                cvCategoryCurug.setCardBackgroundColor(android.graphics.Color.parseColor("#F8FDFB"))
                cvCategoryDanau.setCardBackgroundColor(android.graphics.Color.parseColor("#F8FDFB"))
            }

            if (activeCategory == category) {
                // Klik ulang kategori yang sama -> Hapus filter
                activeCategory = null
                setupPopulerRecyclerView(listWisataPopuler)
                android.widget.Toast.makeText(context, "Filter Kategori Dihapus", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                // Kategori baru terpilih -> Pasang filter
                activeCategory = category
                
                // Highlight warna background card terpilih (hijau toska muda yang estetik)
                val activeColor = android.graphics.Color.parseColor("#E8F8F5")
                binding.apply {
                    when (category) {
                        "Gunung" -> cvCategoryGunung.setCardBackgroundColor(activeColor)
                        "Pantai" -> cvCategoryPantai.setCardBackgroundColor(activeColor)
                        "Curug" -> cvCategoryCurug.setCardBackgroundColor(activeColor)
                        "Danau" -> cvCategoryDanau.setCardBackgroundColor(activeColor)
                    }
                }

                // Filter list popular: Kategori dari API (kategori) atau berdasarkan kata kunci di nama
                val filtered = listWisataPopuler.filter { wisata ->
                    val nama = wisata.nama.lowercase()
                    val kat = (wisata.kategori ?: "").lowercase()
                    when (category) {
                        "Gunung" -> nama.contains("gunung") || nama.contains("kawah") || nama.contains("papandayan") || kat.contains("gunung") || kat.contains("alam")
                        "Pantai" -> nama.contains("pantai") || nama.contains("waterland") || kat.contains("pantai") || kat.contains("air")
                        "Curug" -> nama.contains("curug") || nama.contains("air terjun") || kat.contains("curug")
                        "Danau" -> nama.contains("danau") || nama.contains("situ") || nama.contains("waduk") || kat.contains("danau") || kat.contains("alam")
                        else -> true
                    }
                }
                
                if (filtered.isEmpty()) {
                    android.widget.Toast.makeText(context, "Destinasi kategori $category belum tersedia.", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Menampilkan kategori: $category", android.widget.Toast.LENGTH_SHORT).show()
                }
                setupPopulerRecyclerView(filtered)
            }
        }

        binding.btnCategoryGunung.setOnClickListener { applyCategoryFilter("Gunung") }
        binding.btnCategoryPantai.setOnClickListener { applyCategoryFilter("Pantai") }
        binding.btnCategoryCurug.setOnClickListener { applyCategoryFilter("Curug") }
        binding.btnCategoryDanau.setOnClickListener { applyCategoryFilter("Danau") }
    }

    private fun setupPopulerRecyclerView(list: List<Wisata>) {
        val populerAdapter = PopulerAdapter(list)

        binding.rvPopuler.apply {
            // Set layout menyamping (Horizontal)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = populerAdapter
            setHasFixedSize(true) // Optimasi performa
        }
    }

    private fun fetchWisataFromApi() {
        com.taufan.projectakhir.api.RetrofitClient.instance.getWisata()
            .enqueue(object : retrofit2.Callback<com.taufan.projectakhir.api.WisataResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.taufan.projectakhir.api.WisataResponse>,
                    response: retrofit2.Response<com.taufan.projectakhir.api.WisataResponse>
                ) {
                    if (_binding == null || !isAdded) return
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.status == "success") {
                            val apiWisataList = body.data
                            val mappedList = apiWisataList.map { it.toWisata() }
                            if (mappedList.isNotEmpty()) {
                                listWisataPopuler = mappedList
                                setupPopulerRecyclerView(mappedList)
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.taufan.projectakhir.api.WisataResponse>,
                    t: Throwable
                ) {
                    // Biarkan menggunakan default list jika koneksi gagal
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}