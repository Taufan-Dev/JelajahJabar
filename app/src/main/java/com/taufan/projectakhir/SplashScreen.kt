package com.taufan.projectakhir

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // Global Crash Handler untuk mendeteksi crash sejak awal aplikasi dijalankan
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val intentCrash = Intent(this, LoginActivity::class.java).apply {
                putExtra("CRASH_ERROR", throwable.stackTraceToString())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intentCrash)
            android.os.Process.killProcess(android.os.Process.myPid())
            java.lang.System.exit(10)
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logo = findViewById<ImageView>(R.id.iv_logo)

        // Animasi muncul dari tengah (scale up) selama 100ms
        logo.scaleX = 0f
        logo.scaleY = 0f
        logo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(100)
            .start()

        // Pindah ke MainActivity jika sudah login, atau OnboardingActivity jika belum
        Handler(Looper.getMainLooper()).postDelayed({
            val sessionManager = com.taufan.projectakhir.api.SessionManager(this)
            val intent = if (sessionManager.isLoggedIn()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2000)
    }
}