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

public class UserDAO {
    // Return object polimorfik (Admin atau Kasir)
    public User login(String u, String p) {
        User user = null;
        String sql = "SELECT * FROM users WHERE username=? AND password_hash=? AND is_active=TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String role = rs.getString("role");
                int id = rs.getInt("user_id");
                boolean active = rs.getBoolean("is_active");
                
                if("ADMIN".equals(role)) {
                    user = new Admin(id, u, p, active);
                } else {
                    user = new Kasir(id, u, p, active);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return user;
    }
}
