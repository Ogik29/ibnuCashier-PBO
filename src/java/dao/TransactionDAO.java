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
import model.*;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dimas
 */
public class TransactionDAO {

    private static final Logger LOGGER = Logger.getLogger(TransactionDAO.class.getName());

    public boolean simpanTransaksi(Transaction trx) {
        Connection conn = null;
        PreparedStatement psHead = null;
        PreparedStatement psItem = null;
        PreparedStatement psMove = null;
        PreparedStatement psUpdate = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // 1. Validasi & Kalkulasi Header
            String noStruk = "STR-" + UUID.randomUUID().toString().substring(0, 8);
            trx.hitungTotal();

            String sqlHeader = "INSERT INTO transactions (no_struk, total, kasir_id, metode_bayar) VALUES (?, ?, ?, 'CASH')";
            psHead = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS);
            psHead.setString(1, noStruk);
            psHead.setDouble(2, trx.getTotal());
            psHead.setInt(3, trx.getKasirID());
            psHead.executeUpdate();

            int trxID = 0;
            rs = psHead.getGeneratedKeys();
            if (rs.next()) {
                trxID = rs.getInt(1);
            }

            // 2. Persiapan Query Items & Stok
            String sqlItem = "INSERT INTO transaction_items (transaksi_id, produk_id, qty, harga_satuan, subtotal) VALUES (?,?,?,?,?)";
            psItem = conn.prepareStatement(sqlItem);

            String sqlMove = "INSERT INTO stock_movements (produk_id, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, ?, ?, ?, ?)";
            psMove = conn.prepareStatement(sqlMove);

            String sqlUpdateStok = "UPDATE products SET stok = stok - ? WHERE produk_id = ?";
            psUpdate = conn.prepareStatement(sqlUpdateStok);

            for (SaleItem item : trx.getItems()) {
                // A. Simpan Item
                psItem.setInt(1, trxID);
                psItem.setInt(2, item.getProdukID());
                psItem.setInt(3, item.getQty());
                psItem.setDouble(4, item.getHargaSatuan());
                psItem.setDouble(5, item.getSubtotal());
                psItem.executeUpdate();

                // B. Single Source of Truth (Log Movement)
                psMove.setInt(1, item.getProdukID());
                psMove.setString(2, "OUT_SALE");
                psMove.setInt(3, -item.getQty()); // Negatif karena keluar
                psMove.setString(4, String.valueOf(trxID));
                psMove.setInt(5, trx.getKasirID());
                psMove.executeUpdate();

                // C. Update Realtime Stok
                psUpdate.setInt(1, item.getQty());
                psUpdate.setInt(2, item.getProdukID());
                psUpdate.executeUpdate();
            }

            conn.commit(); // COMMIT SEMUA
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal menyimpan transaksi: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Gagal Rollback", ex);
                }
            }
            return false;
        } finally {
            // Tutup resource manual karena struktur transaksi kompleks
            closeQuietly(rs);
            closeQuietly(psHead);
            closeQuietly(psItem);
            closeQuietly(psMove);
            closeQuietly(psUpdate);
            closeQuietly(conn);
        }
    }

    // Helper untuk menutup resource agar rapi dan tidak nested try-catch berulang
    private void closeQuietly(AutoCloseable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (Exception e) {
            // Ignored
        }
    }
}