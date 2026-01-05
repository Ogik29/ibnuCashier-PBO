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

public class CategoryDAO {
    
    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        try(Connection c = DatabaseConnection.getConnection();
            ResultSet rs = c.createStatement().executeQuery("SELECT * FROM categories")) {
            while(rs.next()) {
                list.add(new Category(rs.getInt("kategori_id"), rs.getString("nama_kategori")));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean add(String nama) {
        String sql = "INSERT INTO categories (nama_kategori) VALUES (?)";
        try(Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nama);
            return ps.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    public boolean delete(int id) {
        try(Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM categories WHERE kategori_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }
}
