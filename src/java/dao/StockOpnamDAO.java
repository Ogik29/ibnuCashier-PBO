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
import java.util.ArrayList;
import java.util.List;

public class StockOpnamDAO {

    // 1. KASIR: Buat Draft Opnam
    public boolean createDraft(int kasirID, List<StockOpnamItem> items) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlHeader = "INSERT INTO stock_opnames (kasir_id_creator, status) VALUES (?, 'DRAFT')";
            PreparedStatement psHead = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS);
            psHead.setInt(1, kasirID);
            psHead.executeUpdate();
            
            int opnamID = 0;
            ResultSet rs = psHead.getGeneratedKeys();
            if(rs.next()) opnamID = rs.getInt(1);

            String sqlItem = "INSERT INTO stock_opname_items (opnam_id, produk_id, qty_fisik, qty_sistem, selisih) VALUES (?,?,?,?,?)";
            PreparedStatement psItem = conn.prepareStatement(sqlItem);

            // Fetch sistem stok saat ini utk dibekukan di laporan
            ProductDAO productDAO = new ProductDAO();

            for(StockOpnamItem item : items) {
                // Ambil stok sistem live
                // Di real world, harus lock table, disini simplified
                Product p = null; 
                // Asumsi items cuma bawa SKU/ID dan QtyFisik dari input kasir
                
                // Kalkulasi
                // selisih = Fisik - Sistem
                // Di contoh sederhana ini, kita asumsikan object item sdh terisi di Controller
                
                psItem.setInt(1, opnamID);
                psItem.setInt(2, item.getProdukID());
                psItem.setInt(3, item.getQtyFisik());
                psItem.setInt(4, item.getQtySistem());
                psItem.setInt(5, item.getQtyFisik() - item.getQtySistem());
                psItem.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { if(conn!=null) conn.rollback(); } catch (SQLException ex){}
            e.printStackTrace();
            return false;
        } finally { try { if(conn!=null) conn.close(); } catch(Exception e){} }
    }

    // 2. ADMIN: Approve Opnam (Update Status to APPROVED)
    public boolean approve(int opnamID, int adminID) {
        String sql = "UPDATE stock_opnames SET status='APPROVED', admin_id_approver=? WHERE opnam_id=? AND status='DRAFT'";
        try(Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, adminID);
            ps.setInt(2, opnamID);
            return ps.executeUpdate() > 0;
        } catch(SQLException e){ e.printStackTrace(); return false; }
    }

    // 3. ADMIN: Post Changes (Terapkan perubahan ke Stok Produk & History)
    public boolean postOpnam(int opnamID, int adminID) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Ambil Items Opnam yg statusnya APPROVED
            String checkStatus = "SELECT status FROM stock_opnames WHERE opnam_id=?";
            PreparedStatement psCheck = conn.prepareStatement(checkStatus);
            psCheck.setInt(1, opnamID);
            ResultSet rsCheck = psCheck.executeQuery();
            if(!rsCheck.next() || !"APPROVED".equals(rsCheck.getString("status"))) {
                return false; // Harus di-approve dulu
            }

            // Ambil detail items
            String getItems = "SELECT * FROM stock_opname_items WHERE opnam_id=?";
            PreparedStatement psItems = conn.prepareStatement(getItems);
            psItems.setInt(1, opnamID);
            ResultSet rsItems = psItems.executeQuery();

            String insertMov = "INSERT INTO stock_movements (produk_id, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, 'ADJUST_OPNAME', ?, ?, ?)";
            PreparedStatement psMov = conn.prepareStatement(insertMov);
            
            String updateStok = "UPDATE products SET stok = stok + ? WHERE produk_id=?";
            PreparedStatement psUpd = conn.prepareStatement(updateStok);

            while(rsItems.next()) {
                int pid = rsItems.getInt("produk_id");
                int selisih = rsItems.getInt("selisih");
                
                if(selisih != 0) {
                    // Insert ke History Movement
                    psMov.setInt(1, pid);
                    psMov.setInt(2, selisih); // + tambah, - kurang
                    psMov.setString(3, "OPNAM-" + opnamID);
                    psMov.setInt(4, adminID);
                    psMov.executeUpdate();

                    // Update Stok Produk Realtime
                    psUpd.setInt(1, selisih);
                    psUpd.setInt(2, pid);
                    psUpd.executeUpdate();
                }
            }

            // Update Header jadi POSTED
            String closeOpnam = "UPDATE stock_opnames SET status='POSTED' WHERE opnam_id=?";
            PreparedStatement psClose = conn.prepareStatement(closeOpnam);
            psClose.setInt(1, opnamID);
            psClose.executeUpdate();

            conn.commit();
            return true;
        } catch(Exception e) {
            try { if(conn!=null) conn.rollback(); } catch(SQLException ex){}
            e.printStackTrace();
            return false;
        } finally { try { if(conn!=null) conn.close(); } catch(Exception e){} }
    }
    
    // Helper: Get Pending Opnames
    public ResultSet getPendingOpnames() throws SQLException {
         Connection conn = DatabaseConnection.getConnection();
         return conn.createStatement().executeQuery(
             "SELECT o.*, u.username as creator FROM stock_opnames o JOIN users u ON o.kasir_id_creator=u.user_id WHERE status='DRAFT'"
         );
    }
}
