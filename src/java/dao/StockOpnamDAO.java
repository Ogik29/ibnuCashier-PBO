/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import config.DatabaseConnection;
import model.StockOpnamItem;
import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StockOpnamDAO {

    private static final Logger LOGGER = Logger.getLogger(StockOpnamDAO.class.getName());

    public boolean createDraft(int kasirID, List<StockOpnamItem> items) {
        Connection conn = null;
        PreparedStatement psHead = null;
        PreparedStatement psItem = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert Header Opnam
            String sqlHead = "INSERT INTO stock_opnames (kasir_id_creator, status, waktu) VALUES (?, 'DRAFT', NOW())";
            psHead = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS);
            psHead.setInt(1, kasirID);
            psHead.executeUpdate();

            int opnamID = 0;
            rs = psHead.getGeneratedKeys();
            if (rs.next()) {
                opnamID = rs.getInt(1);
            }

            // 2. Insert Items Opnam
            String sqlItem = "INSERT INTO stock_opname_items (opnam_id, produk_id, qty_fisik, qty_sistem, selisih) VALUES (?, ?, ?, 0, ?)";
            psItem = conn.prepareStatement(sqlItem);

            for (StockOpnamItem item : items) {
                psItem.setInt(1, opnamID);
                psItem.setInt(2, item.getProdukID());
                // Pada sistem Anda, qty_fisik disini digunakan sebagai 'Penambah/Selisih Stok'
                psItem.setInt(3, item.getQtyFisik()); 
                psItem.setInt(4, item.getQtyFisik()); 
                psItem.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error Create Draft Opnam", e);
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            closeQuietly(rs);
            closeQuietly(psHead);
            closeQuietly(psItem);
            closeQuietly(conn);
        }
    }

    public ResultSet getPendingOpnames() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // UPDATE: Menggunakan GROUP_CONCAT untuk menampilkan detail barang & qty dalam satu kolom
            // Format output: "NamaBarang (Qty), NamaBarang2 (Qty)"
            String sql = "SELECT o.opnam_id, u.username as creator, o.waktu, "
                       + "GROUP_CONCAT(CONCAT(p.nama_produk, ' (', i.qty_fisik, ')') SEPARATOR ', ') as detail_barang "
                       + "FROM stock_opnames o "
                       + "JOIN users u ON o.kasir_id_creator = u.user_id "
                       + "LEFT JOIN stock_opname_items i ON o.opnam_id = i.opnam_id "
                       + "LEFT JOIN products p ON i.produk_id = p.produk_id "
                       + "WHERE o.status = 'DRAFT' "
                       + "GROUP BY o.opnam_id, u.username, o.waktu "
                       + "ORDER BY o.waktu DESC";
                       
            return conn.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal mengambil Pending Opnames", e);
            return null;
        }
    }

    public boolean rejectOpnam(int opnamID, int adminID) {
        String sql = "UPDATE stock_opnames SET status='REJECTED', admin_id_approver=? WHERE opnam_id=?";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminID);
            ps.setInt(2, opnamID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal Reject Opnam", e);
            return false;
        }
    }

    public boolean approveAndPost(int opnamID, int adminID) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Ambil item untuk update stok
            String getItems = "SELECT * FROM stock_opname_items WHERE opnam_id=?";
            try (PreparedStatement psGet = conn.prepareStatement(getItems)) {
                psGet.setInt(1, opnamID);
                try (ResultSet rsItems = psGet.executeQuery()) {
                    
                    String updStok = "UPDATE products SET stok = stok + ? WHERE produk_id=?";
                    PreparedStatement psUpd = conn.prepareStatement(updStok);
                    
                    String insHist = "INSERT INTO stock_movements (produk_id, user_id, tipe_mutasi, qty_change, referensi_id) VALUES (?, ?, 'ADJUST_OPNAM', ?, ?)";
                    PreparedStatement psHist = conn.prepareStatement(insHist);

                    while (rsItems.next()) {
                        int pid = rsItems.getInt("produk_id");
                        // Menggunakan qty_fisik sebagai DELTA (Penambahan/Pengurangan)
                        int tambahQty = rsItems.getInt("qty_fisik"); 

                        if (tambahQty != 0) {
                            // Update Stok Real
                            psUpd.setInt(1, tambahQty);
                            psUpd.setInt(2, pid);
                            psUpd.executeUpdate();

                            // Catat History Mutasi
                            psHist.setInt(1, pid);
                            psHist.setInt(2, adminID);
                            psHist.setInt(3, tambahQty);
                            psHist.setString(4, "OPNAM-" + opnamID);
                            psHist.executeUpdate();
                        }
                    }
                    psUpd.close();
                    psHist.close();
                }
            }

            // 2. Update Status Opnam
            String sqlStat = "UPDATE stock_opnames SET status='APPROVED', admin_id_approver=? WHERE opnam_id=?";
            try(PreparedStatement psDone = conn.prepareStatement(sqlStat)){
                psDone.setInt(1, adminID);
                psDone.setInt(2, opnamID);
                psDone.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal Approve Opnam", e);
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            closeQuietly(conn);
        }
    }
    
    private void closeQuietly(AutoCloseable resource) {
        try { if (resource != null) resource.close(); } catch (Exception e) {}
    }
}