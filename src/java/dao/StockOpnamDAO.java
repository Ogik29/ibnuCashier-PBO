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

public class StockOpnamDAO {

    public boolean createDraft(int kasirID, List<StockOpnamItem> items) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insert Header
            String sqlHead = "INSERT INTO stock_opnames (kasir_id_creator, status) VALUES (?, 'DRAFT')";
            PreparedStatement psHead = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS);
            psHead.setInt(1, kasirID);
            psHead.executeUpdate();
            
            int opnamID = 0;
            ResultSet rs = psHead.getGeneratedKeys();
            if(rs.next()) opnamID = rs.getInt(1);

            // Insert Items
            String sqlItem = "INSERT INTO stock_opname_items (opnam_id, produk_id, qty_fisik, qty_sistem, selisih) VALUES (?, ?, ?, 0, ?)";
            PreparedStatement psItem = conn.prepareStatement(sqlItem);

            for(StockOpnamItem item : items) {
                psItem.setInt(1, opnamID);
                psItem.setInt(2, item.getProdukID());
                psItem.setInt(3, item.getQtyFisik()); 
                psItem.setInt(4, item.getQtyFisik());
                psItem.executeUpdate();
            }
            conn.commit();
            return true;
        } catch(Exception e) { 
            e.printStackTrace(); 
            try{if(conn!=null)conn.rollback();}catch(Exception ex){}
            return false;
        } finally { try{if(conn!=null)conn.close();}catch(Exception ex){} }
    }
    
    // [TETAP SAMA] Get Pending
    public ResultSet getPendingOpnames() throws SQLException {
         Connection conn = DatabaseConnection.getConnection();
         return conn.createStatement().executeQuery(
             "SELECT o.opnam_id, u.username as creator, o.waktu " + 
             "FROM stock_opnames o JOIN users u ON o.kasir_id_creator=u.user_id WHERE status='DRAFT'"
         );
    }

    public boolean rejectOpnam(int opnamID, int adminID) {
        String sql = "UPDATE stock_opnames SET status='REJECTED', admin_id_approver=? WHERE opnam_id=?";
        try(Connection conn = DatabaseConnection.getConnection(); 
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminID);
            ps.setInt(2, opnamID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Menggabungkan Approve dan Update Stok
    public boolean approveAndPost(int opnamID, int adminID) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Ambil detail barang dari Opnam ini
            String getItems = "SELECT * FROM stock_opname_items WHERE opnam_id=?";
            PreparedStatement psGet = conn.prepareStatement(getItems);
            psGet.setInt(1, opnamID);
            ResultSet rsItems = psGet.executeQuery();
            
            // Persiapkan SQL Update & History
            String updStok = "UPDATE products SET stok = stok + ? WHERE produk_id=?";
            PreparedStatement psUpd = conn.prepareStatement(updStok);
            
            String insHist = "INSERT INTO stock_movements (produk_id, user_id, tipe_mutasi, qty_change, referensi_id) VALUES (?, ?, 'ADJUST_OPNAM', ?, ?)";
            PreparedStatement psHist = conn.prepareStatement(insHist);

            while(rsItems.next()) {
                int pid = rsItems.getInt("produk_id");
                int tambahQty = rsItems.getInt("qty_fisik");
                
                if(tambahQty != 0) {
                    // Update Stok (Ditambah)
                    psUpd.setInt(1, tambahQty);
                    psUpd.setInt(2, pid);
                    psUpd.executeUpdate();

                    // Log History
                    psHist.setInt(1, pid);
                    psHist.setInt(2, adminID);
                    psHist.setInt(3, tambahQty);
                    psHist.setString(4, "OPNAM-"+opnamID);
                    psHist.executeUpdate();
                }
            }
            
            // 2. Update Status Header Menjadi APPROVED (Sudah diposting)
            PreparedStatement psDone = conn.prepareStatement("UPDATE stock_opnames SET status='APPROVED', admin_id_approver=? WHERE opnam_id=?");
            psDone.setInt(1, adminID);
            psDone.setInt(2, opnamID);
            psDone.executeUpdate();
            
            conn.commit();
            return true;
        } catch(Exception e) { 
            e.printStackTrace(); 
            try{if(conn!=null)conn.rollback();}catch(Exception ex){}
            return false;
        } finally { try{if(conn!=null)conn.close();}catch(Exception ex){} }
    }
}