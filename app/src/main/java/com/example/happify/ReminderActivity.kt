package com.example.happify

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale

class ReminderActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var adapter: ReminderAdapter? = null
    private var datePicker: EditText? = null
    private var editTextTime: EditText? = null
    private var editTextNotes: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        recyclerView = findViewById(R.id.recyclerView)
        adapter = ReminderAdapter(ArrayList())
        recyclerView.setAdapter(adapter)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.setLayoutManager(layoutManager)

        // Memanggil metode untuk mengambil data dari database dan memperbarui RecyclerView
        loadDataAndRefreshRecyclerView()
        datePicker = findViewById(R.id.datePicker)
        editTextTime = findViewById(R.id.editTextTime)
        editTextNotes = findViewById(R.id.editTextNotes)
        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener { // Ketika ImageView diklik, arahkan ke HomeActivity
            val intent = Intent(this@ReminderActivity, HomeActivity::class.java)
            startActivity(intent)
        }
        datePicker.setOnClickListener(View.OnClickListener { showDatePickerDialog() })
    }

    private fun loadDataAndRefreshRecyclerView() {
        // Ambil data dari database (sesuaikan dengan implementasi koneksi dan query Anda)
        val newData = dataFromDatabase
        // Perbarui RecyclerView dengan data baru
        updateRecyclerView(newData)
    }

    private val dataFromDatabase: List<Reminder>
        private get() =// Implementasi pengambilan data dari database (sesuai dengan database Anda)
// Pastikan ini benar-benar mengambil data terbaru dari database Anda
// Contoh pengambilan data:
            // return yourDatabaseHelper.getAllReminders();
            ArrayList() // Gantilah ini dengan implementasi sesuai dengan database Anda

    private fun updateRecyclerView(newData: List<Reminder>) {
        // Perbarui data pada adapter dan panggil notifyDataSetChanged
        adapter!!.updateData(newData)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(
            this,
            { view: DatePicker?, year1: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Ganti format tanggal langsung menjadi "YYYY-MM-DD"
                val selectedDate = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    year1,
                    monthOfYear + 1,
                    dayOfMonth
                )
                datePicker!!.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    fun showTimePickerDialog(view: View?) {
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val timePickerDialog = TimePickerDialog(
            this,
            { view1: TimePicker?, hourOfDay: Int, minute1: Int ->
                val selectedTime = String.format(
                    Locale.getDefault(), "%02d:%02d", hourOfDay, minute1
                )
                editTextTime!!.setText(selectedTime)
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    // Metode untuk menyimpan reminder
    fun saveReminder(view: View?) {
        val selectedDate = datePicker!!.text.toString()
        val selectedTime = editTextTime!!.text.toString()
        val notes = editTextNotes!!.text.toString()

        // Menyiapkan data untuk dikirim ke server
        val data: MutableMap<String, String> = HashMap()
        data["reminder_date"] = selectedDate
        data["jam"] = selectedTime
        data["notes"] = notes

        // Menjalankan AsyncTask untuk mengirim data ke server
        val saveReminderTask = SaveReminderTask()
        saveReminderTask.execute(data)
    }

    private inner class SaveReminderTask : AsyncTask<Map<String?, String?>?, Void?, String?>() {
        protected override fun doInBackground(vararg params: Map<String?, String?>): String? {
            return if (params.size == 0) null else try {
                val data = params[0]
                val jsonData = prepareJsonData(data)
                val url = URL(Companion.serverUrl)
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
                    "Data berhasil disimpan"
                } else {
                    "Gagal menyimpan data"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "Error: " + e.message)
                "Gagal menyimpan data"
            }
        }

        private fun prepareJsonData(data: Map<String?, String?>): String {
            val jsonData = StringBuilder("{")
            for ((key, value) in data) {
                jsonData.append("\"").append(key).append("\":\"").append(value).append("\",")
            }
            if (jsonData[jsonData.length - 1] == ',') {
                jsonData.deleteCharAt(jsonData.length - 1)
            }
            jsonData.append("}")
            return jsonData.toString()
        }

        override fun onPostExecute(result: String?) {
            Toast.makeText(this@ReminderActivity, result, Toast.LENGTH_SHORT).show()
            try {
                val jsonResponse = JSONObject(result)
                val status = jsonResponse.getString("status")
                val message = jsonResponse.getString("message")
                if ("success" == status) {
                    // Jika penyimpanan berhasil, ambil data terbaru dari server dan perbarui RecyclerView
                    fetchDataFromServer()
                } else if ("error" == status) {
                    // Handle error, display message to user if needed
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("TAG", "JSON Parsing Error: " + e.message)
            }
        }

        companion object {
            private const val serverUrl = "http://10.0.2.2/happify/save_reminder.php"
        }
    }

    private fun fetchDataFromServer() {
        // Implementasi pengambilan data terbaru dari server
        // Setelah mendapatkan data, perbarui RecyclerView
        val newData = dataFromDatabase
        updateRecyclerView(newData)
    }
}