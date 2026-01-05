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
import model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dimas
 */
public class CategoryDAO {
    
    private static final Logger LOGGER = Logger.getLogger(CategoryDAO.class.getName());

    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        
        try (Connection c = DatabaseConnection.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                list.add(new Category(rs.getInt("kategori_id"), rs.getString("nama_kategori")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal Get Categories", e);
        }
        return list;
    }

    public boolean add(String nama) {
        String sql = "INSERT INTO categories (nama_kategori) VALUES (?)";
        try (Connection c = DatabaseConnection.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nama);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal Add Category", e);
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM categories WHERE kategori_id=?";
        try (Connection c = DatabaseConnection.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal Delete Category", e);
            return false;
        }
    }
}
