package com.example.proyectofinal.model

import java.io.Serializable
import java.util.Date

data class Comment(
    val id: Int,
    val articleUrl: String,
    val text: String,
    val userId: Int,
    val username: String,
    val createdAt: Date,
    val likesCount: Int
) : Serializable
