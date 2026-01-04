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

public class TransactionDAO {

    public boolean simpanTransaksi(Transaction trx) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // 1. Validasi & Kalkulasi Header
            String noStruk = "STR-" + UUID.randomUUID().toString().substring(0,8);
            trx.hitungTotal(); 

            String sqlHeader = "INSERT INTO transactions (no_struk, total, kasir_id, metode_bayar) VALUES (?, ?, ?, 'CASH')";
            PreparedStatement psHead = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS);
            psHead.setString(1, noStruk);
            psHead.setDouble(2, trx.getTotal());
            psHead.setInt(3, trx.getKasirID());
            psHead.executeUpdate();

            int trxID = 0;
            ResultSet rs = psHead.getGeneratedKeys();
            if (rs.next()) trxID = rs.getInt(1);

            // 2. Loop Items
            String sqlItem = "INSERT INTO transaction_items (transaksi_id, produk_id, qty, harga_satuan, subtotal) VALUES (?,?,?,?,?)";
            PreparedStatement psItem = conn.prepareStatement(sqlItem);

            String sqlMove = "INSERT INTO stock_movements (produk_id, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psMove = conn.prepareStatement(sqlMove);

            String sqlUpdateStok = "UPDATE products SET stok = stok - ? WHERE produk_id = ?";
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStok);

            for(SaleItem item : trx.getItems()) {
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
            try { if(conn!=null) conn.rollback(); } catch(SQLException ex){}
            e.printStackTrace();
            return false;
        } finally {
             try { if(conn!=null) conn.close(); } catch(SQLException ex){}
        }
    }
}
