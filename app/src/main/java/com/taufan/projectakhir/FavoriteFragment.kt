package com.taufan.projectakhir

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.taufan.projectakhir.databinding.FragmentFavoriteBinding

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoriteBinding.bind(view)

        // Load data riil dari FavoriteManager
        val dataFav = FavoriteManager.getFavorites()

        if (dataFav.isEmpty()) {
            binding.rvFavorite.visibility = View.GONE
            binding.layoutEmptyFavorite.visibility = View.VISIBLE
        } else {
            binding.rvFavorite.visibility = View.VISIBLE
            binding.layoutEmptyFavorite.visibility = View.GONE

            binding.rvFavorite.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = FavoriteAdapter(dataFav)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}