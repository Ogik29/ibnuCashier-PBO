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
import model.StockOpnamItem;
import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dimas
 */
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

            // Insert Header
            String sqlHead = "INSERT INTO stock_opnames (kasir_id_creator, status) VALUES (?, 'DRAFT')";
            psHead = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS);
            psHead.setInt(1, kasirID);
            psHead.executeUpdate();

            int opnamID = 0;
            rs = psHead.getGeneratedKeys();
            if (rs.next()) {
                opnamID = rs.getInt(1);
            }

            // Insert Items
            String sqlItem = "INSERT INTO stock_opname_items (opnam_id, produk_id, qty_fisik, qty_sistem, selisih) VALUES (?, ?, ?, 0, ?)";
            psItem = conn.prepareStatement(sqlItem);

            for (StockOpnamItem item : items) {
                psItem.setInt(1, opnamID);
                psItem.setInt(2, item.getProdukID());
                psItem.setInt(3, item.getQtyFisik());
                psItem.setInt(4, item.getQtyFisik()); // Asumsi awal sistem = fisik sebelum verifikasi
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
        // PERHATIAN: ResultSet dikembalikan terbuka untuk TableModel. 
        // Pastikan caller menutup koneksi atau redesign struktur agar menggunakan List<Object>.
        // Di sini kita tetap mengikuti struktur Anda tapi menambahkan log error.
        try {
            Connection conn = DatabaseConnection.getConnection();
            return conn.createStatement().executeQuery(
                "SELECT o.opnam_id, u.username as creator, o.waktu "
                + "FROM stock_opnames o JOIN users u ON o.kasir_id_creator=u.user_id WHERE status='DRAFT'"
            );
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
        PreparedStatement psGet = null;
        PreparedStatement psUpd = null;
        PreparedStatement psHist = null;
        PreparedStatement psDone = null;
        ResultSet rsItems = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Ambil detail barang dari Opnam ini
            String getItems = "SELECT * FROM stock_opname_items WHERE opnam_id=?";
            psGet = conn.prepareStatement(getItems);
            psGet.setInt(1, opnamID);
            rsItems = psGet.executeQuery();

            String updStok = "UPDATE products SET stok = stok + ? WHERE produk_id=?";
            psUpd = conn.prepareStatement(updStok);

            String insHist = "INSERT INTO stock_movements (produk_id, user_id, tipe_mutasi, qty_change, referensi_id) VALUES (?, ?, 'ADJUST_OPNAM', ?, ?)";
            psHist = conn.prepareStatement(insHist);

            while (rsItems.next()) {
                int pid = rsItems.getInt("produk_id");
                int tambahQty = rsItems.getInt("qty_fisik");

                if (tambahQty != 0) {
                    // Update Stok
                    psUpd.setInt(1, tambahQty);
                    psUpd.setInt(2, pid);
                    psUpd.executeUpdate();

                    // Log History
                    psHist.setInt(1, pid);
                    psHist.setInt(2, adminID);
                    psHist.setInt(3, tambahQty);
                    psHist.setString(4, "OPNAM-" + opnamID);
                    psHist.executeUpdate();
                }
            }

            // 2. Update Status Header
            psDone = conn.prepareStatement("UPDATE stock_opnames SET status='APPROVED', admin_id_approver=? WHERE opnam_id=?");
            psDone.setInt(1, adminID);
            psDone.setInt(2, opnamID);
            psDone.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal Approve Opnam", e);
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            closeQuietly(rsItems);
            closeQuietly(psGet);
            closeQuietly(psUpd);
            closeQuietly(psHist);
            closeQuietly(psDone);
            closeQuietly(conn);
        }
    }
    
    private void closeQuietly(AutoCloseable resource) {
        try { if (resource != null) resource.close(); } catch (Exception e) {}
    }
}