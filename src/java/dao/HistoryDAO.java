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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dimas
 */
public class HistoryDAO {
    
    private static final Logger LOGGER = Logger.getLogger(HistoryDAO.class.getName());

    public List<String[]> getSalesHistory() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT t.no_struk, t.waktu, t.total, u.username "
                   + "FROM transactions t JOIN users u ON t.kasir_id = u.user_id "
                   + "ORDER BY t.waktu DESC";
        
        try (Connection c = DatabaseConnection.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("no_struk"),
                    rs.getString("waktu"),
                    String.valueOf(rs.getDouble("total")),
                    rs.getString("username")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error ambil history penjualan", e);
        }
        return list;
    }

    public List<String[]> getDetailByStruk(String noStruk) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT p.nama_produk, p.sku, ti.qty, ti.harga_satuan, ti.subtotal "
                   + "FROM transaction_items ti "
                   + "JOIN transactions t ON ti.transaksi_id = t.transaksi_id "
                   + "JOIN products p ON ti.produk_id = p.produk_id "
                   + "WHERE t.no_struk = ?";
        
        try (Connection c = DatabaseConnection.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, noStruk);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("sku"),
                        rs.getString("nama_produk"),
                        String.valueOf(rs.getInt("qty")),
                        String.valueOf(rs.getDouble("harga_satuan")),
                        String.valueOf(rs.getDouble("subtotal"))
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error ambil detail struk: " + noStruk, e);
        }
        return list;
    }
}