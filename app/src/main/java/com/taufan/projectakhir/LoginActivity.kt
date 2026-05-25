package com.taufan.projectakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Patterns
import com.taufan.projectakhir.api.LoginRequest
import com.taufan.projectakhir.api.LoginResponse
import com.taufan.projectakhir.api.RetrofitClient
import com.taufan.projectakhir.api.SessionManager
import com.taufan.projectakhir.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Global Crash Handler untuk mendeteksi crash dan mempermudah debugging
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val intentCrash = Intent(this, LoginActivity::class.java).apply {
                putExtra("CRASH_ERROR", throwable.stackTraceToString())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intentCrash)
            android.os.Process.killProcess(android.os.Process.myPid())
            java.lang.System.exit(10)
        }

        // Tampilkan dialog jika mendeteksi crash sebelumnya
        val crashError = intent.getStringExtra("CRASH_ERROR")
        if (crashError != null) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Detail Error (Crash)")
                .setMessage("Mohon maaf, aplikasi terhenti karena error berikut:\n\n$crashError")
                .setCancelable(false)
                .setPositiveButton("Salin Error") { dialog, _ ->
                    val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Crash Log", crashError)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Log disalin! Kirimkan ke developer.", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Tutup") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = "Password minimal 8 karakter"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun loginUser(email: String, password: String) {
        setLoadingState(true)
        val request = LoginRequest(email, password)

        RetrofitClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                setLoadingState(false)
                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    // API mengembalikan: {"status":"success","data":{"user":{...},"token":"..."}}
                    if (loginResponse?.status == "success") {
                        val authData = loginResponse.data
                        val user = authData?.user
                        val token = authData?.token ?: ""
                        val name = user?.name ?: "User"
                        val userEmail = user?.email ?: email
                        val location = when (user?.idWilayah) {
                            2 -> "Bandung"
                            8 -> "Kuningan"
                            else -> "Kuningan"
                        }

                        sessionManager.saveSession(token, name, userEmail, location)

                        Toast.makeText(this@LoginActivity, "Login Berhasil! Selamat datang, $name", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    } else {
                        val pesan = loginResponse?.message ?: "Login gagal, coba lagi"
                        Toast.makeText(this@LoginActivity, pesan, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 401 = email/password salah
                    val pesan = when (response.code()) {
                        401 -> "Email atau password salah"
                        422 -> "Data tidak valid, periksa kembali"
                        else -> "Terjadi kesalahan (${response.code()})"
                    }
                    Toast.makeText(this@LoginActivity, pesan, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                setLoadingState(false)
                Toast.makeText(this@LoginActivity, "Tidak dapat terhubung ke server: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "Mohon tunggu..."
            binding.etEmail.isEnabled = false
            binding.etPassword.isEnabled = false
        } else {
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "Login"
            binding.etEmail.isEnabled = true
            binding.etPassword.isEnabled = true
        }
    }
}