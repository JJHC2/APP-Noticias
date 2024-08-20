package com.example.proyectofinal.model

import java.io.Serializable

data class NewsResponse(
    val articles: List<Article>
)

data class Article(
    val title: String?,
    val author: String?,
    val description: String?,
    val content: String?,
    val url: String?,
    val urlToImage: String?
) : Serializable

data class Source(
    val id: String?,
    val name: String?
) : Serializable
