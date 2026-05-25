package com.taufan.projectakhir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.taufan.projectakhir.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var exploreAdapter: ExploreAdapter
    private lateinit var populerAdapter: PopulerAdapter

    // Data Lengkap sesuai model Wisata yang sudah Parcelable, diinisialisasi kosong (fully dynamic)
    private var listSemuaWisata = emptyList<Wisata>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabLayout()
        fetchWisataFromApi()
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter grid (Explore)
        exploreAdapter = ExploreAdapter(listSemuaWisata)
        binding.rvExplore.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = exploreAdapter
            setHasFixedSize(true)
        }

        // Inisialisasi adapter horizontal (Populer)
        populerAdapter = PopulerAdapter(listSemuaWisata.take(4))
        binding.rvPopuler.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
            adapter = populerAdapter
            setHasFixedSize(true)
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
                                listSemuaWisata = mappedList
                                
                                // Refresh adapter Populer dengan 4 data pertama dari API
                                populerAdapter = PopulerAdapter(mappedList.take(4))
                                binding.rvPopuler.adapter = populerAdapter

                                // Refresh adapter grid explore dengan data terbaru
                                val selectedTabPosition = binding.tabCity.selectedTabPosition
                                val activeCity = if (selectedTabPosition != com.google.android.material.tabs.TabLayout.Tab.INVALID_POSITION) {
                                    binding.tabCity.getTabAt(selectedTabPosition)?.text.toString()
                                } else {
                                    "All"
                                }
                                filterWisata(activeCity)
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.taufan.projectakhir.api.WisataResponse>,
                    t: Throwable
                ) {
                    // Biarkan menggunakan default listSemuaWisata jika koneksi gagal
                }
            })
    }

    private fun setupTabLayout() {
        val cities = listOf("All", "Bandung", "Kuningan", "Garut", "Bogor", "Cirebon")

        // Menghindari duplikasi tab jika fragment di-recreate
        if (binding.tabCity.tabCount == 0) {
            cities.forEach { cityName ->
                binding.tabCity.addTab(binding.tabCity.newTab().setText(cityName))
            }
        }

        binding.tabCity.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedCity = tab?.text.toString()
                filterWisata(selectedCity)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterWisata(kota: String) {
        val hasilFilter = if (kota == "All") {
            listSemuaWisata
        } else {
            listSemuaWisata.filter { it.kota == kota }
        }

        if (hasilFilter.isEmpty()) {
            binding.rvExplore.visibility = View.GONE
            binding.layoutEmptyExplore.visibility = View.VISIBLE
        } else {
            binding.rvExplore.visibility = View.VISIBLE
            binding.layoutEmptyExplore.visibility = View.GONE
        }
        exploreAdapter.updateData(hasilFilter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}