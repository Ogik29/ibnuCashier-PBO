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
import model.Product;
import java.sql.*;

public class ProductDAO {
    public Product getBySku(String sku) {
        String sql = "SELECT * FROM products WHERE sku=?";
        try(Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sku);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return new Product(
                    rs.getInt("produk_id"),
                    rs.getString("sku"),
                    rs.getString("nama_produk"),
                    rs.getDouble("harga_beli"),
                    rs.getDouble("harga_jual"),
                    rs.getInt("stok")
                );
            }
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }
}
