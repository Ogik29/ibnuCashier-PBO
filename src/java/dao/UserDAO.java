/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author dimas
 */
import config.DatabaseConnection;
import model.Admin;
import model.Kasir;
import model.User;
import java.sql.*;
import java.util.logging.Level;     // Tambahan Import
import java.util.logging.Logger;    // Tambahan Import

/**
 *
 * @author dimas
 */
public class UserDAO {
    
    // 1. Inisialisasi Logger untuk menggantikan printStackTrace
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public User login(String username, String password) { // Ubah nama variabel agar jelas (u->username)
        User user = null;
        
        String sql = "SELECT * FROM users WHERE username=? AND password_hash=? AND is_active=TRUE";

        // 2. Try-With-Resources: Resource ditutup otomatis.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password); 

            // 3. Nested Try-With-Resources untuk ResultSet
            // Ini menjamin ResultSet tertutup sempurna dan menghilangkan bug 'Resource Leak'
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    String role = rs.getString("role");
                    int id = rs.getInt("user_id");
                    boolean active = rs.getBoolean("is_active");
                    
                    if("ADMIN".equals(role)) {
                        user = new Admin(id, username, password, active);
                    } else {
                        user = new Kasir(id, username, password, active);
                    }
                }
            }
            
        } catch (SQLException e) {
            // 4. Security Hotspot Fix:
            // Jangan gunakan e.printStackTrace(), gunakan Logger.
            LOGGER.log(Level.SEVERE, "Error saat proses login untuk user: " + username, e);
        }
        
        return user;
    }
}