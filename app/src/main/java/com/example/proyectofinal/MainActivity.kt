package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.adapter.NewsAdapter
import com.example.proyectofinal.api.RetrofitClient
import com.example.proyectofinal.model.Article
import com.example.proyectofinal.model.NewsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var searchEditText: EditText
    private lateinit var logoutButton: Button
    private lateinit var noNewsMessage: TextView
    private val apiKey = "522ad5016c2d48efb16679cbfc403d92"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.news_list_view)
        searchEditText = findViewById(R.id.search_edit_text)
        logoutButton = findViewById(R.id.logout_button)
        noNewsMessage = findViewById(R.id.no_news_message)

        // Realiza la solicitud de noticias
        fetchNews("")

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                fetchNews(query)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        logoutButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchNews(query: String) {
        RetrofitClient.instance.getNews(query, apiKey).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    Log.d("MainActivity", "Received articles: ${articles.size}")
                    if (articles.isEmpty()) {
                        noNewsMessage.visibility = TextView.VISIBLE
                        listView.visibility = ListView.GONE
                    } else {
                        noNewsMessage.visibility = TextView.GONE
                        listView.visibility = ListView.VISIBLE
                        updateListView(articles)
                    }
                } else if (response.code() == 429) {
                    Log.e("MainActivity", "Too many requests. Try again later.")
                    noNewsMessage.text = "Límite de solicitudes alcanzado. Inténtalo de nuevo más tarde."
                    noNewsMessage.visibility = TextView.VISIBLE
                    listView.visibility = ListView.GONE
                } else {
                    Log.e("MainActivity", "Response not successful: ${response.code()}")
                    noNewsMessage.visibility = TextView.VISIBLE
                    listView.visibility = ListView.GONE
                }
            }


            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Log.e("MainActivity", "API call failed: ${t.message}")
                noNewsMessage.visibility = TextView.VISIBLE
                listView.visibility = ListView.GONE
            }
        })
    }

    private fun updateListView(articles: List<Article>) {
        val adapter = NewsAdapter(this, articles)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val article = articles[position]
            Log.d("MainActivity", "Article URL: ${article.url}")
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("article", article)
            }
            startActivity(intent)
        }
    }
}
