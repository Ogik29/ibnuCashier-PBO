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

    public List<Product> searchProducts(String keyword, int catFilter) {
        List<Product> list = new ArrayList<>();
        // JOIN agar nama_kategori terambil
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, c.nama_kategori " +
            "FROM products p LEFT JOIN categories c ON p.kategori_id = c.kategori_id " +
            "WHERE 1=1 "
        );

        if(keyword != null && !keyword.isEmpty()) {
            sql.append("AND (p.sku LIKE ? OR p.nama_produk LIKE ?) ");
        }
        if(catFilter > 0) {
            sql.append("AND p.kategori_id = ? ");
        }
        sql.append("ORDER BY p.nama_produk ASC");

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if(keyword != null && !keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            if(catFilter > 0) ps.setInt(idx, catFilter);

            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                // Memasukkan data ke Model Product yang BARU
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
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }
    
    public Product getBySku(String sku) {
        List<Product> res = searchProducts(sku, 0); 
        for(Product p : res) {
            if(p.getSKU().equalsIgnoreCase(sku)) return p;
        }
        return null;
    }

    public List<Product> getAllProducts() {
        return searchProducts(null, 0);
    }
    
    public boolean insertProduct(Product p, int catId) {
        String sql = "INSERT INTO products(sku,nama_produk,harga_jual,kategori_id,stok,harga_beli) VALUES(?,?,?,?,0,0)";
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1, p.getSKU());
            ps.setString(2, p.getNamaProduk());
            ps.setDouble(3, p.getHargaJual());
            ps.setInt(4, catId);
            return ps.executeUpdate() > 0;
        } catch(Exception e){ return false; }
    }
    
    public boolean updateProduct(Product p, int kategoriID) {
        String sql = "UPDATE products SET nama_produk=?, harga_jual=?, kategori_id=? WHERE sku=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNamaProduk());
            ps.setDouble(2, p.getHargaJual());
            ps.setInt(3, kategoriID);
            ps.setString(4, p.getSKU());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
    
    public boolean deleteProduct(String sku) {
         try(Connection c=DatabaseConnection.getConnection(); PreparedStatement ps=c.prepareStatement("DELETE FROM products WHERE sku=?")) {
            ps.setString(1, sku);
            return ps.executeUpdate() > 0;
        } catch(Exception e){ return false; }
    }
}