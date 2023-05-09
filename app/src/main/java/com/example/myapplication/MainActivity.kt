package com.example.myapplication

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var editTextWords: EditText
    private var wordsResponse: List<String> = emptyList()
    private var haiku: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextWords = findViewById<EditText>(R.id.editTextWords)
        val button = findViewById<Button>(R.id.submit_button)
        button.setOnClickListener {
            val words = extractWords()
            thread(true) {
                wordsResponse = words.map { fetch(it) }
                haiku = generateHaiku(wordsResponse)
                Log.d("Haiku", haiku.toString())
            }
        }

        val haikuButton = findViewById<Button>(R.id.button2)
        haikuButton.setOnClickListener {
            if (haiku.isNotEmpty()) {
                val randomHaiku = haiku.shuffled().first()
                Toast.makeText(this, randomHaiku, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please submit words first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun extractWords(): List<String> {
        val words = editTextWords.text.toString().split(" ")
        if (words.size != 3) {
            Toast.makeText(this, "Your input must be 3 words", Toast.LENGTH_SHORT).show()
        }
        return words
    }

    private fun fetch(word: String): String {
        val inputStream: InputStream
        var result: String = ""
        try {
            // Create URL
            val uri = Uri.Builder()
                .scheme("https")
                .authority("rhymebrain.com")
                .appendPath("talk")
                .appendQueryParameter("function", "getRhymes")
                .appendQueryParameter("word", word)
                .build()
            val url = URL(uri.toString())

            // Create HttpURLConnection
            val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            // Launch GET request
            conn.connect()
            // Receive response as inputStream
            inputStream = conn.inputStream

            result = if (inputStream != null)
            // Convert input stream to string
                inputStream.bufferedReader().use(BufferedReader::readText)
            else
                "error: inputStream is nULl"
        } catch (err: Error) {
            print("Error when executing get request:" + err.localizedMessage)
        }
        return result
    }

    private fun generateHaiku(responses: List<String>): List<String> {
        val haiku = mutableListOf<String>()
        for (i in 5 downTo 1) {
            val words = mutableListOf<String>()
            Log.d("Array", responses.toString())
            val data = JSONObject(responses.toString())
            responses.forEach {
                val json = JSONObject(it.toString())
                val syllables = json.getInt("syllables")
                val word = json.getString("word")
                if (syllables == i) {
                    words.add(word)
                }
            }
            if (words.isEmpty()) {
                Toast.makeText(this, "Could not generate haiku with given words", Toast.LENGTH_SHORT).show()
                return emptyList()
            }
            haiku.add(words.shuffled().first())
        }
        return haiku
    }


}
