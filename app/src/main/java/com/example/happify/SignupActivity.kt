package com.example.happify

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SignupActivity : AppCompatActivity() {
    private var fullNameEditText: EditText? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val signInText = findViewById<TextView>(R.id.signInText)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        // Create SpannableString to give color to the text "Sign In"
        val spannableString = SpannableString("Already have an account? Sign In")
        val startIndex = spannableString.toString().indexOf("Sign In")
        val endIndex = startIndex + "Sign In".length
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#FF4081")),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        signInText.text = spannableString
    }

    fun navigateToSignInActivity(view: View?) {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

    fun createAccount(view: View?) {
        val fullName = fullNameEditText!!.text.toString().trim { it <= ' ' }
        val email = emailEditText!!.text.toString().trim { it <= ' ' }
        val password = passwordEditText!!.text.toString().trim { it <= ' ' }
        val role = "user" // You might want to set the role based on your logic
        if (!fullName.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            val databaseConnection = DatabaseConnection()
            databaseConnection.execute(fullName, email, password, role)
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class DatabaseConnection : AsyncTask<String?, Void?, Boolean>() {
        protected override fun doInBackground(vararg params: String): Boolean {
            val fullName = params[0]
            val email = params[1]
            val password = params[2]
            val role = params[3]
            try {
                // URL server PHP
                val url = URL("http://10.0.2.2/happify/create_account.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                val postData: MutableMap<String, String> = HashMap()
                postData["fullName"] = fullName
                postData["email"] = email
                postData["password"] = password
                postData["role"] = role
                val os = conn.outputStream
                os.write(getPostDataString(postData).toByteArray())
                os.flush()
                os.close()
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Reading response from input Stream
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val result = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                    reader.close()
                    Log.d("DatabaseConnection", "Server Response: $result")
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        //   INI TAHAN 3 DETIK TOAST
        override fun onPostExecute(success: Boolean) {
            if (success) {
                Log.d("DatabaseConnection", "onPostExecute: true")
                Handler().postDelayed({
                    Toast.makeText(this@SignupActivity, "Akun berhasil dibuat", Toast.LENGTH_SHORT)
                        .show()
                    Handler().postDelayed({
                        val intent = Intent(this@SignupActivity, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 1000)
                }, 1000)
            } else {
                // Proses gagal
                Log.d("DatabaseConnection", "onPostExecute: false")
            }
        }

        private fun getPostDataString(params: Map<String, String>): String {
            val result = StringBuilder()
            var first = true
            try {
                for ((key, value) in params) {
                    if (first) {
                        first = false
                    } else {
                        result.append("&")
                    }
                    result.append(URLEncoder.encode(key, "UTF-8"))
                    result.append("=")
                    result.append(URLEncoder.encode(value, "UTF-8"))
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return result.toString()
        }
    }
}