package com.taufan.projectakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Patterns
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        // Global Crash Handler for debugging convenience
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val intentCrash = Intent(this, LoginActivity::class.java).apply {
                putExtra("CRASH_ERROR", throwable.stackTraceToString())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intentCrash)
            android.os.Process.killProcess(android.os.Process.myPid())
            java.lang.System.exit(10)
        }

        val crashError = intent.getStringExtra("CRASH_ERROR")
        if (crashError != null) {
            showCrashDialog(crashError)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        // Updated ID to match modern layout: tvSignup -> tv_signup
        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun showCrashDialog(error: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("System Recovery")
            .setMessage("The application recovered from an unexpected error. Would you like to copy the log?")
            .setPositiveButton("Copy Log") { _, _ ->
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Crash Log", error)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Dismiss", null)
            .show()
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email address is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = "Min. 8 characters"
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
                    if (loginResponse?.status == "success") {
                        val authData = loginResponse.data
                        val user = authData?.user
                        val token = authData?.token ?: ""
                        val name = user?.name ?: "Guest"
                        val userEmail = user?.email ?: email
                        val location = if (user?.idWilayah == 2) "Bandung" else "Kuningan"

                        sessionManager.saveSession(token, name, userEmail, location)

                        Toast.makeText(this@LoginActivity, "Welcome back, $name!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val msg = when (response.code()) {
                        401 -> "Invalid credentials"
                        else -> "Server error (${response.code()})"
                    }
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                setLoadingState(false)
                Toast.makeText(this@LoginActivity, "Connection failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "Authenticating..." else "Masuk"
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }
}
