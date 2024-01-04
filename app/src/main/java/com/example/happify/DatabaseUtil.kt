package com.example.happify

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseUtil {
    @get:Throws(SQLException::class)
    val connection: Connection
        get() = DriverManager.getConnection(
            "jdbc:mysql://192.168.18.243:3306/db_happify",
            "root",
            ""
        )
}