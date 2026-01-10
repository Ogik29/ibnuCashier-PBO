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
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

public class StockOpnamDAO {

    private static final Logger LOGGER = Logger.getLogger(StockOpnamDAO.class.getName());

    public boolean createDraft(int kasirID, List<StockOpnamItem> items) {
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert Header Opnam dengan Try-With-Resources (TWR)
            int opnamID = 0;
            String sqlHead = "INSERT INTO stock_opnames (kasir_id_creator, status, waktu) VALUES (?, 'DRAFT', NOW())";
            
            try (PreparedStatement psHead = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS)) {
                psHead.setInt(1, kasirID);
                psHead.executeUpdate();
                try (ResultSet rs = psHead.getGeneratedKeys()) {
                    if (rs.next()) opnamID = rs.getInt(1);
                }
            }

            if (opnamID == 0) throw new SQLException("Gagal mendapatkan ID Opnam");

            // 2. Insert Items Opnam (Multiple resources TWR)
            String sqlItem = "INSERT INTO stock_opname_items (opnam_id, produk_id, qty_fisik, qty_sistem, selisih) VALUES (?, ?, ?, 0, ?)";
            
            try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                for (StockOpnamItem item : items) {
                    psItem.setInt(1, opnamID);
                    psItem.setInt(2, item.getProdukID());
                    // Pada sistem Anda, qty_fisik disini digunakan sebagai 'Penambah/Selisih Stok'
                    psItem.setInt(3, item.getQtyFisik()); 
                    psItem.setInt(4, item.getQtyFisik()); 
                    psItem.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error Create Draft Opnam", e);
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            closeQuietly(conn); // Helper manual close for connection only
        }
    }

    /**
     * Mengambil list Pending Opnames untuk Admin Dashboard.
     * Menggunakan CachedRowSet agar connection aman ditutup.
     */
    public ResultSet getPendingOpnames() {
        String sql = "SELECT o.opnam_id, u.username as creator, o.waktu, "
                   + "GROUP_CONCAT(CONCAT(p.nama_produk, ' (', i.qty_fisik, ')') SEPARATOR ', ') as detail_barang "
                   + "FROM stock_opnames o "
                   + "JOIN users u ON o.kasir_id_creator = u.user_id "
                   + "LEFT JOIN stock_opname_items i ON o.opnam_id = i.opnam_id "
                   + "LEFT JOIN products p ON i.produk_id = p.produk_id "
                   + "WHERE o.status = 'DRAFT' "
                   + "GROUP BY o.opnam_id, u.username, o.waktu "
                   + "ORDER BY o.waktu DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            @SuppressWarnings("resource")
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            return crs;

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

    /**
     * Memperbaiki error "Use try-with-resources" pada approveAndPost.
     * Menyatukan 3 PreparedStatement ke dalam satu blok TWR.
     */
    public boolean approveAndPost(int opnamID, int adminID) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String getItems = "SELECT * FROM stock_opname_items WHERE opnam_id=?";
            String updStok  = "UPDATE products SET stok = stok + ? WHERE produk_id=?";
            String insHist  = "INSERT INTO stock_movements (produk_id, user_id, tipe_mutasi, qty_change, referensi_id) VALUES (?, ?, 'ADJUST_OPNAM', ?, ?)";
            String sqlStat  = "UPDATE stock_opnames SET status='APPROVED', admin_id_approver=? WHERE opnam_id=?";

            // 1. Grouped Try-With-Resources untuk resource Transaksi
            try (PreparedStatement psGet = conn.prepareStatement(getItems);
                 PreparedStatement psUpd = conn.prepareStatement(updStok);
                 PreparedStatement psHist = conn.prepareStatement(insHist);
                 PreparedStatement psDone = conn.prepareStatement(sqlStat)) {

                // A. Loop Items & Update Stok
                psGet.setInt(1, opnamID);
                try (ResultSet rsItems = psGet.executeQuery()) {
                    while (rsItems.next()) {
                        int pid = rsItems.getInt("produk_id");
                        // Menggunakan qty_fisik sebagai DELTA
                        int tambahQty = rsItems.getInt("qty_fisik"); 

                        if (tambahQty != 0) {
                            // Update Stok
                            psUpd.setInt(1, tambahQty);
                            psUpd.setInt(2, pid);
                            psUpd.executeUpdate();

                            // Catat History
                            psHist.setInt(1, pid);
                            psHist.setInt(2, adminID);
                            psHist.setInt(3, tambahQty);
                            psHist.setString(4, "OPNAM-" + opnamID);
                            psHist.executeUpdate();
                        }
                    }
                }

                // B. Update Status Opnam ke APPROVED
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
    
    // Helper untuk menutup resource non-AutoCloseable dengan aman (terutama Connection yang dibuat manual di atas)
    private void closeQuietly(AutoCloseable resource) {
        try { if (resource != null) resource.close(); } catch (Exception e) {}
    }
}