package com.example.proyectofinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyectofinal.model.Article
import com.example.proyectofinal.model.Comment
import com.example.proyectofinal.repository.CommentRepository
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.concurrent.thread

class DetailActivity : AppCompatActivity() {

    private lateinit var commentRepository: CommentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val article = intent.getSerializableExtra("article") as? Article ?: run {
            finish()
            return
        }

        val detailImage: ImageView = findViewById(R.id.detail_image)
        val detailTitle: TextView = findViewById(R.id.detail_title)
        val detailAuthor: TextView = findViewById(R.id.detail_author)
        val detailDescription: TextView = findViewById(R.id.detail_description)
        val detailContent: TextView = findViewById(R.id.detail_content)
        val moreInfoButton: ImageButton = findViewById(R.id.more_info_button)
        val backButton: ImageButton = findViewById(R.id.back_button)
        val postCommentButton: Button = findViewById(R.id.post_comment_button)
        val commentInput: EditText = findViewById(R.id.comment_input)

        commentRepository = CommentRepository()

        detailTitle.text = article.title ?: "Título no disponible"
        detailAuthor.text = article.author ?: "Autor no disponible"
        detailDescription.text = article.description ?: "Descripción no disponible"
        detailContent.text = article.content ?: "Contenido no disponible"

        Glide.with(this)
            .load(article.urlToImage)
            .placeholder(R.drawable.placeholder)
            .into(detailImage)

        moreInfoButton.setOnClickListener {
            article.url?.let { url ->
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            } ?: run {
                Toast.makeText(this, "URL no disponible", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        val shareButton = findViewById<ImageButton>(R.id.share_button)
        shareButton.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, article.url)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir noticia con:"))
        }

        postCommentButton.setOnClickListener {
            val commentText = commentInput.text.toString()
            if (commentText.isNotBlank()) {
                article.url?.let { url ->
                    postComment(url, commentText)
                    commentInput.text.clear()
                }
            } else {
                Toast.makeText(this, "El comentario no puede estar vacío", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        article.url?.let { url -> loadComments(url) }
    }

    private fun loadComments(articleUrl: String) {
        val commentsList: LinearLayout = findViewById(R.id.comments_list)
        commentsList.removeAllViews()
        thread {
            val comments = commentRepository.getCommentsByArticleUrl(articleUrl)
            val userId = getUserDetails().first ?: return@thread
            runOnUiThread {
                comments.forEach { comment ->
                    val commentView = layoutInflater.inflate(R.layout.comment_item, commentsList, false)
                    val commentTextView = commentView.findViewById<TextView>(R.id.comment_text)
                    val commentInfoTextView = commentView.findViewById<TextView>(R.id.comment_info)
                    val likesCountTextView = commentView.findViewById<TextView>(R.id.likes_count)
                    val likeButton = commentView.findViewById<ImageButton>(R.id.like_button)

                    commentTextView.text = comment.text
                    commentInfoTextView.text = "Usuario: ${comment.username} - Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(comment.createdAt)}"
                    likesCountTextView.text = "${comment.likesCount} likes"


                    val hasLiked = commentRepository.hasUserLikedComment(comment.id, userId)
                    likeButton.setImageResource(if (hasLiked) R.drawable.ic_like_red else R.drawable.ic_like_white)


                    likeButton.setOnClickListener {
                        if (hasLiked) {
                            thread {
                                val success = commentRepository.removeLike(comment.id, userId)
                                if (success) {
                                    runOnUiThread {
                                        val newLikesCount = commentRepository.getLikesCount(comment.id)
                                        likesCountTextView.text = "$newLikesCount likes"
                                        likeButton.setImageResource(R.drawable.ic_like_white)
                                    }
                                }
                            }
                        } else {
                            thread {
                                val success = commentRepository.addLike(comment.id, userId)
                                if (success) {
                                    runOnUiThread {
                                        val newLikesCount = commentRepository.getLikesCount(comment.id)
                                        likesCountTextView.text = "$newLikesCount likes"
                                        likeButton.setImageResource(R.drawable.ic_like_red)
                                    }
                                }
                            }
                        }
                    }

                    commentsList.addView(commentView)
                }
            }
        }
    }


    private fun getUserDetails(): Pair<Int?, String?> {
        val sharedPref = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val userIdString = sharedPref.getString("userId", null)
        val username = sharedPref.getString("username", null)
        return Pair(userIdString?.toIntOrNull(), username)
    }

    private fun postComment(articleUrl: String, comment: String) {
        val (userId, username) = getUserDetails()
        val newComment = Comment(
            id = 0,
            articleUrl = articleUrl,
            text = comment,
            userId = userId ?: 0,
            username = username ?: "Desconocido",
            createdAt = java.util.Date(),
            likesCount = 0
        )
        thread {
            val success = commentRepository.addComment(articleUrl, newComment)
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Comentario enviado", Toast.LENGTH_SHORT).show()
                    loadComments(articleUrl)
                } else {
                    Toast.makeText(this, "Error al enviar comentario", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
