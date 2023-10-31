package com.example.bookApi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getBookssDatabaseConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        }
        catch( ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return DriverManager.getConnection("jdbc:mariadb://localhost:3306/books", "root", "root");
    }
}