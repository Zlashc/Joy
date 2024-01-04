package com.example.happify

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
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

class HomeActivity : AppCompatActivity() {
    private var textHelloUser: TextView? = null
    private var userEmail: String? = null
    private var fullName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        textHelloUser = findViewById(R.id.textHelloUser)
        val pelacakMoodButton = findViewById<Button>(R.id.pelacakMood)
        pelacakMoodButton.setOnClickListener { view: View? ->
            val intent = Intent(this@HomeActivity, PelacakMoodActivity::class.java)
            startActivity(intent)
        }
        val jurnalismeButton = findViewById<Button>(R.id.Jurnalisme)
        jurnalismeButton.setOnClickListener { view: View? ->
            val intent = Intent(this@HomeActivity, JurnalismeActivity::class.java)
            startActivity(intent)
        }

        // Retrieve fullName from SharedPreferences
        val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        fullName = preferences.getString("fullName", "")
        if (!fullName!!.isEmpty()) {
            textHelloUser.setText("Hello, $fullName")
        }
        val extras = intent.extras
        if (extras != null) {
            userEmail = extras.getString("userEmail")
            if (userEmail != null && !userEmail!!.isEmpty() && fullName!!.isEmpty()) {
                GetUserFullNameTask().execute(userEmail)
            }
        }
        val navbarIcon = findViewById<ImageView>(R.id.navbar)
        navbarIcon.setOnClickListener { v -> showPopupMenu(v) }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menu_user) {
                val intent = Intent(this@HomeActivity, UserProfileActivity::class.java)
                startActivity(intent)
                true
            } else if (item.itemId == R.id.menu_reminder) {
                // Handle reminder menu click
                val intent = Intent(this@HomeActivity, ReminderActivity::class.java)
                startActivity(intent)
                true
            } else if (item.itemId == R.id.menu_MoodTracker) {
                // Handle reminder menu click
                val intent = Intent(this@HomeActivity, MoodTrackerActivity::class.java)
                startActivity(intent)
                true
            } else if (item.itemId == R.id.menu_logout) {
                // Handle logout menu click
                logout()
                true
            } else {
                false
            }
        }
        popupMenu.show()
    }

    private fun logout() {
        // Implement your logout logic here
        // For example, clearing SharedPreferences and navigating to the login screen
        val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        preferences.edit().clear().apply()
        val intent = Intent(this@HomeActivity, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private inner class GetUserFullNameTask : AsyncTask<String?, Void?, String?>() {
        protected override fun doInBackground(vararg params: String): String? {
            val userEmail = params[0]
            var fullName: String? = null
            try {
                val url = URL("http://10.0.2.2/happify/get_user_info.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                val postData: MutableMap<String, String> = HashMap()
                postData["email"] = userEmail
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
                    Log.d("Server Response", result.toString())
                    val jsonObject = JSONObject(result.toString())
                    Log.d("JSON Object", jsonObject.toString())
                    if (jsonObject.getString("status") == "success") {
                        fullName = jsonObject.getString("fullName")

                        // Save full_name to SharedPreferences
                        val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                        val editor = preferences.edit()
                        editor.putString("fullName", fullName)
                        editor.apply()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return fullName
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

        override fun onPostExecute(fullName: String?) {
            if (fullName != null && !fullName.isEmpty()) {
                textHelloUser!!.text = "Hello, $fullName"
            } else {
                Toast.makeText(
                    this@HomeActivity,
                    "Failed to retrieve full name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}