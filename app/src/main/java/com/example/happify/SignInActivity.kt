package com.example.happify

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SignInActivity : AppCompatActivity() {
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        val signUpText = findViewById<TextView>(R.id.signUpText)
        val spannableString = SpannableString("Already have an account? Sign Up")
        val startIndex = spannableString.toString().indexOf("Sign Up")
        val endIndex = startIndex + "Sign Up".length
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#FF4081")),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        signUpText.text = spannableString
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
    }

    fun navigateToSignUpActivity(view: View?) {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    fun signIn(view: View?) {
        val email = emailEditText!!.text.toString().trim { it <= ' ' }
        val password = passwordEditText!!.text.toString().trim { it <= ' ' }
        if (!email.isEmpty() && !password.isEmpty()) {
            val authenticationTask = AuthenticationTask()
            authenticationTask.execute(email, password)
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class AuthenticationTask : AsyncTask<String?, Void?, String?>() {
        protected override fun doInBackground(vararg params: String): String? {
            val email = params[0]
            val password = params[1]
            try {
                val url = URL("http://10.0.2.2/happify/authenticate_user.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                val postData: MutableMap<String, String> = HashMap()
                postData["email"] = email
                postData["password"] = password
                val os = conn.outputStream
                os.write(getPostDataString(postData).toByteArray())
                os.flush()
                os.close()
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val result = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                    reader.close()
                    val jsonObject = JSONObject(result.toString())
                    if (jsonObject.getString("status") == "success") {
                        // Save full_name to SharedPreferences
                        val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                        val editor = preferences.edit()
                        editor.putString("fullName", jsonObject.getString("fullName"))
                        editor.apply()
                        return email
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(email: String?) {
            if (email != null) {
                val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                intent.putExtra("userEmail", email)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@SignInActivity, "Email / Password Salah!", Toast.LENGTH_SHORT)
                    .show()
            }
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