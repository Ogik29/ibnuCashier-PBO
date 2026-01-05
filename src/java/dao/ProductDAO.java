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
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // [ADMIN] Menambah Produk Baru
    public boolean insertProduct(Product p) {
        String sql = "INSERT INTO products (sku, nama_produk, harga_beli, harga_jual, stok, kategori_id) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getSKU());
            ps.setString(2, p.getNamaProduk());
            ps.setDouble(3, 0); // Default 0, logic harga beli biasanya di Faktur
            ps.setDouble(4, p.getHargaJual());
            ps.setInt(5, 0); // Stok awal 0, nambah lewat Faktur
            ps.setInt(6, 1); // Default kategori 1 sementara
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // [ADMIN] Update Produk
    public boolean updateProduct(Product p) {
        String sql = "UPDATE products SET nama_produk=?, harga_jual=? WHERE sku=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNamaProduk());
            ps.setDouble(2, p.getHargaJual());
            ps.setString(3, p.getSKU());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // [SHARED] Get All Products
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY nama_produk ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()){
                list.add(new Product(
                    rs.getInt("produk_id"), rs.getString("sku"), rs.getString("nama_produk"),
                    rs.getDouble("harga_beli"), rs.getDouble("harga_jual"), rs.getInt("stok")
                ));
            }
        } catch(Exception e){ e.printStackTrace(); }
        return list;
    }
    
    // [SHARED] Get Single by SKU (Untuk Scan)
    public Product getBySku(String sku) {
        String sql = "SELECT * FROM products WHERE sku=?";
        try(Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sku);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return new Product(rs.getInt("produk_id"), rs.getString("sku"), rs.getString("nama_produk"), rs.getDouble("harga_beli"), rs.getDouble("harga_jual"), rs.getInt("stok"));
        } catch(Exception e){ e.printStackTrace(); }
        return null;
    }
}
