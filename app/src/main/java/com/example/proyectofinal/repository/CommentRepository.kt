package com.example.proyectofinal.repository

import com.example.proyectofinal.model.Comment
import com.example.proyectofinal.ConnectionBD.ConnectionBD
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp

class CommentRepository {

    private val connectionBD = ConnectionBD()

    private fun getConnection(): Connection? {
        return connectionBD.connect()
    }

    fun getCommentsByArticleUrl(articleUrl: String): List<Comment> {
        val comments = mutableListOf<Comment>()
        val query = """
        SELECT c.id, c.articleUrl, c.text, c.user_id, u.usuario AS username, c.created_at, COALESCE(cl.likes_count, 0) AS likes_count
        FROM comments c
        JOIN usuario u ON c.user_id = u.id
        LEFT JOIN (
            SELECT comment_id, COUNT(*) AS likes_count
            FROM comment_like
            GROUP BY comment_id
        ) cl ON c.id = cl.comment_id
        WHERE c.articleUrl = ?
        """

        var connection: Connection? = null
        var resultSet: ResultSet? = null
        var statement: PreparedStatement? = null

        try {
            connection = getConnection()
            statement = connection?.prepareStatement(query)
            statement?.setString(1, articleUrl)
            resultSet = statement?.executeQuery()

            while (resultSet?.next() == true) {
                val comment = Comment(
                    id = resultSet.getInt("id"),
                    articleUrl = resultSet.getString("articleUrl"),
                    text = resultSet.getString("text"),
                    userId = resultSet.getInt("user_id"),
                    username = resultSet.getString("username"),
                    createdAt = resultSet.getTimestamp("created_at"),
                    likesCount = resultSet.getInt("likes_count")
                )
                comments.add(comment)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }

        return comments
    }

    fun addComment(articleUrl: String, comment: Comment): Boolean {
        val query = "INSERT INTO comments (articleUrl, text, user_id, created_at) VALUES (?, ?, ?, ?)"

        var connection: Connection? = null
        var statement: PreparedStatement? = null

        return try {
            connection = getConnection()
            statement = connection?.prepareStatement(query)
            statement?.setString(1, articleUrl)
            statement?.setString(2, comment.text)
            statement?.setInt(3, comment.userId)
            statement?.setTimestamp(4, Timestamp(comment.createdAt.time))

            val rowsAffected = statement?.executeUpdate() ?: 0
            rowsAffected > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        } finally {
            statement?.close()
            connection?.close()
        }
    }

    fun addLike(commentId: Int, userId: Int): Boolean {
        // Verifica si el like ya existe
        if (hasUserLikedComment(commentId, userId)) {
            return false // El like ya existe, no se realiza la inserción
        }

        val query = "INSERT INTO comment_like (comment_id, user_id) VALUES (?, ?)"
        var connection: Connection? = null
        var statement: PreparedStatement? = null

        return try {
            connection = getConnection()
            statement = connection?.prepareStatement(query)
            statement?.setInt(1, commentId)
            statement?.setInt(2, userId)

            val rowsAffected = statement?.executeUpdate() ?: 0
            rowsAffected > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        } finally {
            statement?.close()
            connection?.close()
        }
    }

    fun removeLike(commentId: Int, userId: Int): Boolean {
        val query = "DELETE FROM comment_like WHERE comment_id = ? AND user_id = ?"
        var connection: Connection? = null
        var statement: PreparedStatement? = null

        return try {
            connection = getConnection()
            statement = connection?.prepareStatement(query)
            statement?.setInt(1, commentId)
            statement?.setInt(2, userId)

            val rowsAffected = statement?.executeUpdate() ?: 0
            rowsAffected > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        } finally {
            statement?.close()
            connection?.close()
        }
    }

    fun getLikesCount(commentId: Int): Int {
        val query = "SELECT COUNT(*) AS likes_count FROM comment_like WHERE comment_id = ?"
        var connection: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        return try {
            connection = getConnection()
            statement = connection?.prepareStatement(query)
            statement?.setInt(1, commentId)
            resultSet = statement?.executeQuery()

            if (resultSet?.next() == true) {
                resultSet.getInt("likes_count")
            } else {
                0
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            0
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
    }

    fun hasUserLikedComment(commentId: Int, userId: Int): Boolean {
        val query = "SELECT COUNT(*) AS count FROM comment_like WHERE comment_id = ? AND user_id = ?"
        var connection: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        return try {
            connection = getConnection()
            statement = connection?.prepareStatement(query)
            statement?.setInt(1, commentId)
            statement?.setInt(2, userId)
            resultSet = statement?.executeQuery()

            if (resultSet?.next() == true) {
                resultSet.getInt("count") > 0
            } else {
                false
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
    }
}
