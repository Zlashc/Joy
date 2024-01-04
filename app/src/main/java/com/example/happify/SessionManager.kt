package com.example.happify

import android.content.Context
import android.content.SharedPreferences

// SessionManager.java
class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun loginUser(fullName: String?, email: String?, password: String?) {
        editor.putString(KEY_FULL_NAME, fullName)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PASSWORD, password)
        editor.apply()
    }

    val isLoggedIn: Boolean
        get() = sharedPreferences.getString(KEY_EMAIL, null) != null

    fun saveUserData(fullName: String?, email: String?, password: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_FULL_NAME, fullName)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PASSWORD, password)
        editor.apply()
    }

    fun logoutUser() {
        editor.clear()
        editor.apply()
    }

    val fullName: String?
        get() = sharedPreferences.getString(KEY_FULL_NAME, "")
    val email: String?
        get() = sharedPreferences.getString(KEY_EMAIL, "")
    val password: String?
        get() = sharedPreferences.getString(KEY_PASSWORD, "")

    companion object {
        private const val PREF_NAME = "UserSession"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }
}