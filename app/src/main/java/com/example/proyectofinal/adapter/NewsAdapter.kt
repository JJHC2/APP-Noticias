package com.example.proyectofinal.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.proyectofinal.R
import com.example.proyectofinal.model.Article

class NewsAdapter(private val context: Context, private val articles: List<Article>) : ArrayAdapter<Article>(context, 0, articles) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val article = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

        val titleTextView: TextView = view.findViewById(R.id.news_title)
        val authorTextView: TextView = view.findViewById(R.id.news_author)
        val thumbnailImageView: ImageView = view.findViewById(R.id.news_thumbnail)

        titleTextView.text = article?.title ?: "Título no disponible"
        authorTextView.text = article?.author ?: "Autor no disponible"

        val imageUrl = article?.urlToImage
        if (imageUrl.isNullOrEmpty()) {
            thumbnailImageView.setImageResource(R.drawable.placeholder)
        } else {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(thumbnailImageView)
        }

        return view
    }
}
