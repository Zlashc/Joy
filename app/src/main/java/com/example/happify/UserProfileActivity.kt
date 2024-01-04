package com.example.happify

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// UserProfileActivity.java
class UserProfileActivity : AppCompatActivity() {
    private var sessionManager: SessionManager? = null
    private var editTextFullName: EditText? = null
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        sessionManager = SessionManager(applicationContext)
        editTextFullName = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)

        // Check if the user is logged in
        if (sessionManager!!.isLoggedIn) {
            // Retrieve user data from SharedPreferences
            val fullName = sessionManager.getFullName()
            val email = sessionManager.getEmail()
            val password = sessionManager.getPassword()

            // Populate the EditText fields with user data
            editTextFullName.setText(fullName)
            editTextEmail.setText(email)
            editTextPassword.setText(password)
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener { v: View? ->
            // Navigate back to HomeActivity
            finish()
        }
    }
}