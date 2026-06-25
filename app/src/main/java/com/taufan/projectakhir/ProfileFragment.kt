package com.taufan.projectakhir

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.taufan.projectakhir.api.SessionManager
import com.taufan.projectakhir.databinding.ActivityProfileFragmentBinding

class ProfileFragment : Fragment() {

    private var _binding: ActivityProfileFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Load data dari SessionManager
        binding.etProfileName.setText(sessionManager.getUserName())
        binding.etProfileEmail.setText(sessionManager.getUserEmail())
        
        // Handle Dark Mode Switch State
        val isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = isDarkMode
    }

    private fun setupListeners() {
        binding.btnTiketSaya.setOnClickListener {
            val intent = Intent(requireContext(), TicketHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.btnSimpan.setOnClickListener {
            val newName = binding.etProfileName.text.toString().trim()
            val newEmail = binding.etProfileEmail.text.toString().trim()

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(requireContext(), "Nama dan Email tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sessionManager.updateProfile(newName, newEmail)
            (activity as? MainActivity)?.updateHeader()
            Toast.makeText(requireContext(), "Profil Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogoutProfile.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Keluar Aplikasi")
            .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                logout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun logout() {
        val token = sessionManager.getToken()
        if (!token.isNullOrEmpty()) {
            com.taufan.projectakhir.api.RetrofitClient.instance.logout("Bearer $token")
                .enqueue(object : retrofit2.Callback<com.taufan.projectakhir.api.SimpleResponse> {
                    override fun onResponse(call: retrofit2.Call<com.taufan.projectakhir.api.SimpleResponse>, response: retrofit2.Response<com.taufan.projectakhir.api.SimpleResponse>) {}
                    override fun onFailure(call: retrofit2.Call<com.taufan.projectakhir.api.SimpleResponse>, t: Throwable) {}
                })
        }
        sessionManager.clearSession()
        Toast.makeText(requireContext(), "Anda telah keluar dari aplikasi 👋", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finishAffinity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}