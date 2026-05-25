package com.taufan.projectakhir

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.taufan.projectakhir.R
import com.taufan.projectakhir.api.SessionManager

class ProfileFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile_fragment, container, false)
        sessionManager = SessionManager(requireContext())

        val etName = view.findViewById<EditText>(R.id.etProfileName)
        val etEmail = view.findViewById<EditText>(R.id.etProfileEmail)
        val etUsername = view.findViewById<EditText>(R.id.etProfileUsername)
        val etPhone = view.findViewById<EditText>(R.id.etProfilePhone)
        
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)
        val btnLogout = view.findViewById<Button>(R.id.btnLogoutProfile)

        // Load data dari SessionManager
        etName.setText(sessionManager.getUserName())
        etEmail.setText(sessionManager.getUserEmail())
        // Username & phone bisa diset default / static atau disave di prefs juga
        etUsername.setText(sessionManager.getUserName()?.replace(" ", "")?.lowercase() ?: "taufanganteng")

        btnSimpan.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(requireContext(), "Nama dan Email tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sessionManager.updateProfile(newName, newEmail)
            
            // Update header di MainActivity secara langsung
            (activity as? MainActivity)?.updateHeader()

            Toast.makeText(requireContext(), "Profil Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Keluar Aplikasi")
                .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
                .setPositiveButton("Ya, Keluar") { _, _ ->
                    sessionManager.clearSession()
                    Toast.makeText(requireContext(), "Anda telah keluar dari aplikasi 👋", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    activity?.finishAffinity()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        return view
    }
}