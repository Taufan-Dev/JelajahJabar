package com.taufan.projectakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taufan.projectakhir.api.RegisterRequest
import com.taufan.projectakhir.api.RegisterResponse
import com.taufan.projectakhir.api.RetrofitClient
import com.taufan.projectakhir.databinding.ActivitySignUpBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Patterns

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Updated ID: btnRegister -> btnSignup
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(name, email, password)) {
                registerUser(name, email, password)
            }
        }

        // Updated ID: tvSignin -> tvLogin
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        // btnBack is correctly defined in activity_sign_up.xml
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.tilName.error = "Nama lengkap wajib diisi"
            isValid = false
        } else {
            binding.tilName.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email wajib diisi"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password wajib diisi"
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = "Password minimal 8 karakter"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun registerUser(name: String, email: String, password: String) {
        setLoadingState(true)
        val request = RegisterRequest(name, email, password)

        RetrofitClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                setLoadingState(false)

                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    if (registerResponse?.status == "success") {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Registrasi Berhasil! Silakan Login",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    } else {
                        val pesan = registerResponse?.message ?: "Registrasi gagal, coba lagi"
                        Toast.makeText(this@SignUpActivity, pesan, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    when (response.code()) {
                        422 -> {
                            val errorBody = response.errorBody()?.string()
                            if (errorBody != null && errorBody.contains("password")) {
                                binding.tilPassword.error = "Password minimal 8 karakter"
                            } else if (errorBody != null && errorBody.contains("email")) {
                                binding.tilEmail.error = "Email sudah terdaftar"
                            } else {
                                Toast.makeText(this@SignUpActivity, "Data tidak valid, periksa kembali", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else -> {
                            Toast.makeText(this@SignUpActivity, "Terjadi kesalahan (${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                setLoadingState(false)
                Toast.makeText(this@SignUpActivity, "Tidak dapat terhubung ke server: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            // Updated ID: btnRegister -> btnSignup
            binding.btnSignup.isEnabled = false
            binding.btnSignup.text = "Mohon tunggu..."
            binding.etName.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.etPassword.isEnabled = false
        } else {
            // Updated ID: btnRegister -> btnSignup
            binding.btnSignup.isEnabled = true
            binding.btnSignup.text = "Daftar Sekarang"
            binding.etName.isEnabled = true
            binding.etEmail.isEnabled = true
            binding.etPassword.isEnabled = true
        }
    }
}
