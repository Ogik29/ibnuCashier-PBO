/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/db_ibnu_cashier";
    private static final String USER = System.getProperty("db.user") != null ? System.getProperty("db.user") : "root";
    private static final String PASS = System.getProperty("db.password") != null ? System.getProperty("db.password") : "root";

    // Private constructor (Lolos SonarQube "Hide implicit public constructor")
    private DatabaseConnection() {}

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(JDBC_DRIVER);
            
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Koneksi Database Gagal: {0}", e.getMessage());
        }
        return conn;
    }
}
