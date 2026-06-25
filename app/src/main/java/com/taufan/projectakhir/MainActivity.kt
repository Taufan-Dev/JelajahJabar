package com.taufan.projectakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taufan.projectakhir.api.SessionManager
import com.taufan.projectakhir.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        // Tampilkan data user dari sesi yang tersimpan
        updateHeader()

        // Logika Logout
        binding.tvLogOut?.setOnClickListener {
            logoutUser()
        }

        // Load HomeFragment sebagai tampilan awal
        loadFragment(HomeFragment())

        // Handling Navigasi Bawah
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    binding.headerContent.visibility = android.view.View.VISIBLE
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_explore -> {
                    binding.headerContent.visibility = android.view.View.GONE
                    loadFragment(ExploreFragment())
                    true
                }
                R.id.navigation_favorite -> {
                    binding.headerContent.visibility = android.view.View.GONE
                    loadFragment(FavoriteFragment())
                    true
                }
                R.id.navigation_profile -> {
                    binding.headerContent.visibility = android.view.View.GONE
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Set tab Home sebagai aktif secara default
        binding.bottomNavigation.selectedItemId = R.id.navigation_home

        // Pindah ke Favorite otomatis jika dikirim dari DetailActivity
        val openFavorite = intent.getBooleanExtra("OPEN_FAVORITE", false)
        if (openFavorite) {
            moveToFavorite()
        }
    }

    fun updateHeader() {
        // Menggunakan ID langsung yang telah kita tambahkan pada layout
        binding.tvGreeting.text = "Wilujeng Sumping, ${sessionManager.getUserName()} 👋"
        binding.tvUserLocation.text = "📍 ${sessionManager.getUserLocation()}"
    }

    override fun onResume() {
        super.onResume()
        updateHeader()
    }

    private fun logoutUser() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                val token = sessionManager.getToken()
                if (!token.isNullOrEmpty()) {
                    com.taufan.projectakhir.api.RetrofitClient.instance.logout("Bearer $token")
                        .enqueue(object : retrofit2.Callback<com.taufan.projectakhir.api.SimpleResponse> {
                            override fun onResponse(
                                call: retrofit2.Call<com.taufan.projectakhir.api.SimpleResponse>,
                                response: retrofit2.Response<com.taufan.projectakhir.api.SimpleResponse>
                            ) {}
                            override fun onFailure(
                                call: retrofit2.Call<com.taufan.projectakhir.api.SimpleResponse>,
                                t: Throwable
                            ) {}
                        })
                }
                sessionManager.clearSession()
                Toast.makeText(this, "Anda telah keluar dari aplikasi 👋", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finishAffinity()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Fungsi inti untuk menukar Fragment di fragment_container
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Fungsi navigasi ke Explore (bisa dipanggil dari fragment lain)
    fun moveToExplore() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_explore
    }

    // Fungsi navigasi ke Favorite (bisa dipanggil dari fragment lain)
    fun moveToFavorite() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_favorite
    }
}