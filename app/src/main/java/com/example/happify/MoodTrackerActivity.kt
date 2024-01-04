package com.example.happify

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MoodTrackerActivity : AppCompatActivity() {
    private var editTextQuestion1: EditText? = null
    private var editTextQuestion2: EditText? = null
    private var editTextQuestion3: EditText? = null
    private var editTextQuestion4: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_tracker)
        editTextQuestion1 = findViewById(R.id.editTextQuestion1)
        editTextQuestion2 = findViewById(R.id.editTextQuestion2)
        editTextQuestion3 = findViewById(R.id.editTextQuestion3)
        editTextQuestion4 = findViewById(R.id.editTextQuestion4)
        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            // Navigate back to HomeActivity
            val intent = Intent(this@MoodTrackerActivity, HomeActivity::class.java)
            startActivity(intent)
            finish() // Optional: finish the current activity to remove it from the back stack
        }
        val sendButton = findViewById<Button>(R.id.sendButton)
        sendButton.setOnClickListener { sendMoodData() }
    }

    private fun sendMoodData() {
        val question1 = editTextQuestion1!!.text.toString()
        val question2 = editTextQuestion2!!.text.toString()
        val question3 = editTextQuestion3!!.text.toString()
        val question4 = editTextQuestion4!!.text.toString()

        // Menyiapkan data untuk dikirim ke server
        val data: MutableMap<String, String> = HashMap()
        data["pertanyaan_1"] = question1
        data["pertanyaan_2"] = question2
        data["pertanyaan_3"] = question3
        data["pertanyaan_4"] = question4

        // Menjalankan AsyncTask untuk mengirim data ke server
        val sendMoodDataTask = SendMoodDataTask()
        sendMoodDataTask.execute(data)

        // Menampilkan pesan sukses atau gagal
        Toast.makeText(this, "Mengirim pertanyaan...", Toast.LENGTH_SHORT).show()
    }

    private inner class SendMoodDataTask : AsyncTask<Map<String?, String?>?, Void?, String?>() {
        protected override fun doInBackground(vararg params: Map<String?, String?>): String? {
            return if (params.size == 0) null else try {
                val data = params[0]
                val jsonData = prepareJsonData(data)
                val url = URL(SERVER_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                val os = connection.outputStream
                os.write(jsonData.toByteArray(charset("UTF-8")))
                os.flush()
                os.close()
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    "Pertanyaan berhasil dikirim"
                } else {
                    "Gagal mengirim pertanyaan"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Gagal mengirim pertanyaan"
            }
        }

        private fun prepareJsonData(data: Map<String?, String?>): String {
            val jsonObject = JSONObject(data)
            return jsonObject.toString()
        }

        override fun onPostExecute(result: String?) {
            // Menampilkan pesan hasil operasi (berhasil/gagal)
            Toast.makeText(this@MoodTrackerActivity, result, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val SERVER_URL = "http://10.0.2.2/happify/insert_mood.php"
    }
}