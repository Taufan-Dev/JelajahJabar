package com.taufan.projectakhir.api

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "jelajah_jabar_pref"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_LOCATION = "user_location"
    }

    fun saveSession(token: String, name: String, email: String, location: String?) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_TOKEN, token)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_LOCATION, location ?: "Kuningan")
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, "User")
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, "")
    }

    fun getUserLocation(): String {
        return prefs.getString(KEY_USER_LOCATION, "Kuningan") ?: "Kuningan"
    }

    fun updateProfile(name: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
