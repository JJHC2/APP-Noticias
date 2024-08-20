package com.example.proyectofinal.ConnectionBD

import android.annotation.SuppressLint
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager


class ConnectionBD {
    private val ip = "192.168.136.121"
    private val usuario = "sa"
    private val password = "123456789"
    private val basedatos = "api_app"

    @SuppressLint("NewApi")
    fun connect(): Connection? {
        var connection: Connection? = null
        var connectionURL: String? = null
        try {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            connectionURL =
                "jdbc:jtds:sqlserver://$ip/$basedatos;user=$usuario;password=$password;"
            connection = DriverManager.getConnection(connectionURL)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Error de conexion SQL: ", e.message!!)
        }

        return connection
    }

}