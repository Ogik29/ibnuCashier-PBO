/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import config.DatabaseConnection; // Pastikan ini sesuai package DB Anda (util/config)
// Jika di kode sebelumnya namanya 'util.DBConnection', sesuaikan baris di atas.
// Asumsi sesuai snippet sebelumnya: import util.DBConnection;

import model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    public List<Product> searchProducts(String keyword, int catFilter) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.nama_kategori "
                + "FROM products p LEFT JOIN categories c ON p.kategori_id = c.kategori_id "
                + "WHERE 1=1 "
        );

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (p.sku LIKE ? OR p.nama_produk LIKE ?) ");
        }
        if (catFilter > 0) {
            sql.append("AND p.kategori_id = ? ");
        }
        sql.append("ORDER BY p.nama_produk ASC");

        try (Connection conn = config.DatabaseConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            if (catFilter > 0) {
                ps.setInt(idx, catFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Product(
                            rs.getInt("produk_id"),
                            rs.getString("sku"),
                            rs.getString("nama_produk"),
                            rs.getDouble("harga_beli"),
                            rs.getDouble("harga_jual"),
                            rs.getInt("stok"),
                            rs.getString("nama_kategori"),
                            rs.getInt("kategori_id")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal searchProducts", e);
        }
        return list;
    }

    public Product getBySku(String sku) {
        List<Product> res = searchProducts(sku, 0);
        for (Product p : res) {
            if (p.getSKU().equalsIgnoreCase(sku)) {
                return p;
            }
        }
        return null;
    }

    public List<Product> getAllProducts() {
        return searchProducts(null, 0);
    }

    // UPDATE: Menambahkan input STOK
    public boolean insertProduct(Product p, int catId) {
        // Default harga beli 0, stok sesuai input
        String sql = "INSERT INTO products(sku,nama_produk,harga_jual,kategori_id,stok,harga_beli) VALUES(?,?,?,?,?,0)";
        try (Connection c = config.DatabaseConnection.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getSKU());
            ps.setString(2, p.getNamaProduk());
            ps.setDouble(3, p.getHargaJual());
            ps.setInt(4, catId);
            ps.setInt(5, p.getStok()); // Input stok
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal Insert Product", e);
            return false;
        }
    }

    // UPDATE: Update Product termasuk STOK
    public boolean updateProduct(Product p, int kategoriID) {
        String sql = "UPDATE products SET nama_produk=?, harga_jual=?, kategori_id=?, stok=? WHERE sku=?";
        try (Connection conn = config.DatabaseConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNamaProduk());
            ps.setDouble(2, p.getHargaJual());
            ps.setInt(3, kategoriID);
            ps.setInt(4, p.getStok()); // Update Stok
            ps.setString(5, p.getSKU());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal Update Product", e);
            return false;
        }
    }

    public boolean deleteProduct(String sku) {
        // Gunakan SKU
        try (Connection c = config.DatabaseConnection.getConnection(); 
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE sku=?")) {
            ps.setString(1, sku);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal Delete Product. Mungkin produk sudah dipakai di transaksi.", e);
            return false;
        }
    }
}