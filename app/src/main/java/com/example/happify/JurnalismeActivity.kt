package com.example.happify

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class JurnalismeActivity : AppCompatActivity() {
    private val serverUrl = "http://10.0.2.2/happify/insert_journal.php"
    private var fullName: String? = null
    private var journalInput: EditText? = null
    private var alertDialog: AlertDialog? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: JournalAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jurnalisme)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        adapter = JournalAdapter()
        recyclerView.setAdapter(adapter)
        val luapkanEmosimuButton = findViewById<Button>(R.id.luapkanEmosimuButton)
        luapkanEmosimuButton.setOnClickListener { showJournalModal() }
        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener { // Navigate back to HomeActivity
            val intent = Intent(this@JurnalismeActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showJournalModal() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val modalView = inflater.inflate(R.layout.modal_journal, null)
        val textHelloUser = modalView.findViewById<TextView>(R.id.textHelloUser)

        // Retrieve full name from SharedPreferences
        val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        fullName = preferences.getString("fullName", "")

        // Check for null or empty full name
        if (fullName != null && !fullName!!.isEmpty()) {
            textHelloUser.text = "Hello, $fullName"
            journalInput = modalView.findViewById(R.id.journalInput)
            val sendButton = modalView.findViewById<Button>(R.id.sendButton)
            sendButton.setOnClickListener {
                val journalContent = journalInput.getText().toString()
                val postData = JSONObject()
                try {
                    postData.put("full_name", fullName)
                    postData.put("content", journalContent)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("TAG", "Error: " + e.message)
                }
                SendDataToServerTask().execute(serverUrl, postData.toString())
                alertDialog!!.dismiss()
            }
            builder.setView(modalView)
            alertDialog = builder.create()
            alertDialog!!.show()
        } else {
            Toast.makeText(this, "Full name not available", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class SendDataToServerTask : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String): String {
            val serverUrl = params[0]
            val jsonData = params[1]
            try {
                val url = URL(serverUrl)
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
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val result = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                    reader.close()
                    return result.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "Error: " + e.message)
            }
            return "Error sending data to server"
        }

        override fun onPostExecute(result: String) {
            Log.d("Server Response", result)
            try {
                val jsonResponse = JSONObject(result)
                val status = jsonResponse.getString("status")
                val message = jsonResponse.getString("message")
                if ("success" == status) {
                    Toast.makeText(
                        this@JurnalismeActivity,
                        "Pesan berhasil terkirim",
                        Toast.LENGTH_SHORT
                    ).show()
                    adapter!!.addJournalEntry(fullName, journalInput!!.text.toString())
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("TAG", "Error parsing JSON: " + e.message)
            }
        }
    }

    private class JournalAdapter : RecyclerView.Adapter<JournalAdapter.ViewHolder>() {
        private val entries: MutableList<JournalEntry>

        init {
            entries = ArrayList()
        }

        fun addJournalEntry(fullName: String?, content: String) {
            entries.add(JournalEntry(fullName, content, System.currentTimeMillis()))
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_journal, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = entries[position]
            holder.text.setText(entry.getFullName() + ": " + entry.getContent())
        }

        override fun getItemCount(): Int {
            return entries.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var text: TextView

            init {
                text = itemView.findViewById(R.id.entryTextView)
            }
        }

        private class JournalEntry(val fullName: String?, val content: String, val timestamp: Long)
    }
}