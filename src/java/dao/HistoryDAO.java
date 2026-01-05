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
import model.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {
    public List<String[]> getSalesHistory() {
        List<String[]> list = new ArrayList<>();
        // Join Transaksi dengan User
        String sql = "SELECT t.no_struk, t.waktu, t.total, u.username " +
                     "FROM transactions t JOIN users u ON t.kasir_id = u.user_id " +
                     "ORDER BY t.waktu DESC";
        try(Connection c = DatabaseConnection.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {
            while(rs.next()) {
                list.add(new String[]{
                    rs.getString("no_struk"),
                    rs.getString("waktu"),
                    String.valueOf(rs.getDouble("total")),
                    rs.getString("username")
                });
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String[]> getDetailByStruk(String noStruk) {
        List<String[]> list = new ArrayList<>();
        // Join Detail dengan Produk
        String sql = "SELECT p.nama_produk, p.sku, ti.qty, ti.harga_satuan, ti.subtotal " +
                     "FROM transaction_items ti " +
                     "JOIN transactions t ON ti.transaksi_id = t.transaksi_id " +
                     "JOIN products p ON ti.produk_id = p.produk_id " +
                     "WHERE t.no_struk = ?";
        try(Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, noStruk);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                list.add(new String[]{
                    rs.getString("sku"),
                    rs.getString("nama_produk"),
                    String.valueOf(rs.getInt("qty")),
                    String.valueOf(rs.getDouble("harga_satuan")),
                    String.valueOf(rs.getDouble("subtotal"))
                });
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }
}
