/*
 * DAO ini menangani logika Pembelian (Restock) dan Pengurangan Stok (Kasir)
 */
package dao;

import java.sql.*;
import model.FakturPembelian;
import model.FakturPembelianItem;
import model.Product;
import config.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;

public class PembelianDAO {

    /**
     * FITUR 1: SIMPAN RESTOCK
     * Menjalankan 4 query sekaligus:
     * 1. Insert Faktur, 2. Insert Items, 3. Update Stok, 4. Log Mutasi
     */
    public boolean simpanRestock(FakturPembelian faktur, int adminID) {
        Connection conn = null;
        PreparedStatement psFaktur = null, psItem = null, psUpdateStok = null, psLog = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Simpan Header Faktur
            String sqlFaktur = "INSERT INTO faktur_pembelian (no_faktur, waktu, status, admin_id_approver) VALUES (?, NOW(), 'APPROVED', ?)";
            psFaktur = conn.prepareStatement(sqlFaktur, Statement.RETURN_GENERATED_KEYS);
            psFaktur.setString(1, faktur.getNoFaktur());
            psFaktur.setInt(2, adminID);
            psFaktur.executeUpdate();

            // Ambil ID yang baru saja dibuat
            int fakturId = 0;
            ResultSet rs = psFaktur.getGeneratedKeys();
            if (rs.next()) {
                fakturId = rs.getInt(1);
            }

            // 2. Loop semua item
            for (FakturPembelianItem item : faktur.getItems()) {
                // a. Simpan Detail Item
                String sqlItem = "INSERT INTO faktur_items (faktur_id, produk_id, qty, harga_beli_satuan) VALUES (?, ?, ?, ?)";
                psItem = conn.prepareStatement(sqlItem);
                psItem.setInt(1, fakturId);
                psItem.setInt(2, item.getProdukID());
                psItem.setInt(3, item.getQty());
                psItem.setDouble(4, item.getHargaBeliSatuan());
                psItem.executeUpdate();

                // b. Update Master Produk (Stok Nambah + Harga Beli/HPP Update)
                String sqlProd = "UPDATE products SET stok = stok + ?, harga_beli = ? WHERE produk_id = ?";
                psUpdateStok = conn.prepareStatement(sqlProd);
                psUpdateStok.setInt(1, item.getQty());
                psUpdateStok.setDouble(2, item.getHargaBeliSatuan());
                psUpdateStok.setInt(3, item.getProdukID());
                psUpdateStok.executeUpdate();

                // c. Catat Mutasi (Log Stok)
                String sqlMove = "INSERT INTO stock_movements (produk_id, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, 'IN_PURCHASE', ?, ?, ?)";
                psLog = conn.prepareStatement(sqlMove);
                psLog.setInt(1, item.getProdukID());
                psLog.setInt(2, item.getQty()); // Positif
                psLog.setString(3, String.valueOf(fakturId));
                psLog.setInt(4, adminID);
                psLog.executeUpdate();
            }

            conn.commit(); // Commit Transaction
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {} // Rollback jika error
            return false;
        } finally {
            closeResources(conn, psFaktur, psItem, psUpdateStok);
        }
    }
    
    // Helper Pencarian Produk untuk Form Restock & Kasir
    public Product cariProdukByScan(String keyword) {
        Product p = null;
        String sql = "SELECT * FROM products WHERE sku = ? OR produk_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, keyword);
            // Cek apakah keyword angka untuk pencarian by ID
            try { ps.setInt(2, Integer.parseInt(keyword)); } catch(Exception e) { ps.setInt(2, -1); }
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                p = new Product();
                p.setProdukID(rs.getInt("produk_id"));
                p.setSku(rs.getString("sku"));
                p.setNamaProduk(rs.getString("nama_produk"));
                p.setStok(rs.getInt("stok"));
                p.setHargaBeli(rs.getDouble("harga_beli"));
                p.setHargaJual(rs.getDouble("harga_jual"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }
    
    // Method 1: KASIR INPUT BARANG KE FAKTUR (INCREMENTAL / ITEM PER ITEM)
public boolean inputItemRestockKasir(String noFaktur, String sku, int qty, double hargaBeli, int idKasir) {
    Connection conn = null;
    try {
        conn = config.DatabaseConnection.getConnection();
        conn.setAutoCommit(false);

        // 1. Cek apakah No Faktur DRAFT sudah ada?
        int fakturId = 0;
        String sqlCheck = "SELECT faktur_id FROM faktur_pembelian WHERE no_faktur = ? AND status = 'DRAFT'";
        PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
        psCheck.setString(1, noFaktur);
        ResultSet rs = psCheck.executeQuery();
        
        if (rs.next()) {
            fakturId = rs.getInt("faktur_id");
        } else {
            // Jika belum ada atau status bukan draft, Buat Faktur Baru
            String sqlHead = "INSERT INTO faktur_pembelian (no_faktur, waktu, status, admin_id_approver) VALUES (?, NOW(), 'DRAFT', NULL)";
            PreparedStatement psHead = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS);
            psHead.setString(1, noFaktur);
            psHead.executeUpdate();
            ResultSet rsKey = psHead.getGeneratedKeys();
            if(rsKey.next()) fakturId = rsKey.getInt(1);
        }

        // 2. Cari Produk ID dari SKU
        int produkId = 0;
        PreparedStatement psProd = conn.prepareStatement("SELECT produk_id FROM products WHERE sku = ?");
        psProd.setString(1, sku);
        ResultSet rsProd = psProd.executeQuery();
        if(rsProd.next()) produkId = rsProd.getInt("produk_id");
        else return false; // Produk tidak ada

        // 3. Masukkan Item ke Database
        String sqlItem = "INSERT INTO faktur_items (faktur_id, produk_id, qty, harga_beli_satuan) VALUES (?, ?, ?, ?)";
        PreparedStatement psItem = conn.prepareStatement(sqlItem);
        psItem.setInt(1, fakturId);
        psItem.setInt(2, produkId);
        psItem.setInt(3, qty);
        psItem.setDouble(4, hargaBeli);
        psItem.executeUpdate();

        conn.commit();
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        try { if(conn!=null) conn.rollback(); } catch(Exception ex){}
        return false;
    } finally {
        try { if(conn!=null) conn.close(); } catch(Exception ex){}
    }
}

// Method 2: ADMIN APPROVE FAKTUR (BARU STOK BERTAMBAH DISINI)
public boolean approveFaktur(int fakturId, int adminId) {
    Connection conn = null;
    try {
        conn = config.DatabaseConnection.getConnection();
        conn.setAutoCommit(false);

        // 1. Ambil Semua Item di Faktur tsb
        String sqlGetItems = "SELECT * FROM faktur_items WHERE faktur_id = ?";
        PreparedStatement psGet = conn.prepareStatement(sqlGetItems);
        psGet.setInt(1, fakturId);
        ResultSet rsItems = psGet.executeQuery();

        while(rsItems.next()) {
            int pid = rsItems.getInt("produk_id");
            int qty = rsItems.getInt("qty");
            double harga = rsItems.getDouble("harga_beli_satuan");

            // 2. Update Stok & Harga Beli Master
            String sqlUpdate = "UPDATE products SET stok = stok + ?, harga_beli = ? WHERE produk_id = ?";
            PreparedStatement psUpd = conn.prepareStatement(sqlUpdate);
            psUpd.setInt(1, qty);
            psUpd.setDouble(2, harga);
            psUpd.setInt(3, pid);
            psUpd.executeUpdate();

            // 3. Log Movement
            String sqlMove = "INSERT INTO stock_movements (produk_id, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, 'IN_PURCHASE', ?, ?, ?)";
            PreparedStatement psLog = conn.prepareStatement(sqlMove);
            psLog.setInt(1, pid);
            psLog.setInt(2, qty);
            psLog.setString(3, String.valueOf(fakturId));
            psLog.setInt(4, adminId);
            psLog.executeUpdate();
        }

        // 4. Update Status Faktur
        String sqlStatus = "UPDATE faktur_pembelian SET status = 'APPROVED', admin_id_approver = ? WHERE faktur_id = ?";
        PreparedStatement psStat = conn.prepareStatement(sqlStatus);
        psStat.setInt(1, adminId);
        psStat.setInt(2, fakturId);
        psStat.executeUpdate();

        conn.commit();
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        try { if(conn!=null) conn.rollback(); } catch(Exception ex){}
        return false;
    } finally {
         try { if(conn!=null) conn.close(); } catch(Exception ex){}
    }
}

// Method 3: REJECT
public boolean rejectFaktur(int fakturId, int adminId) {
     try (Connection conn = config.DatabaseConnection.getConnection();
          PreparedStatement ps = conn.prepareStatement("UPDATE faktur_pembelian SET status='REJECTED', admin_id_approver=? WHERE faktur_id=?")) {
         ps.setInt(1, adminId);
         ps.setInt(2, fakturId);
         return ps.executeUpdate() > 0;
     } catch (Exception e) { return false; }
}

// Method 4: List Draft untuk Admin
public ResultSet getDraftInvoices() throws SQLException {
     // PERHATIAN: Pemanggilan harus ditutup resultsetnya di JSP
     Connection conn = config.DatabaseConnection.getConnection();
     Statement stmt = conn.createStatement();
     // Group by agar tampil per faktur + total item
     String sql = "SELECT f.faktur_id, f.no_faktur, f.waktu, COUNT(i.id) as jum_item, SUM(i.qty * i.harga_beli_satuan) as total_nilai " +
                  "FROM faktur_pembelian f LEFT JOIN faktur_items i ON f.faktur_id = i.faktur_id " +
                  "WHERE f.status = 'DRAFT' GROUP BY f.faktur_id ORDER BY f.waktu DESC";
     return stmt.executeQuery(sql);
}

public List<FakturPembelianItem> getDetailFaktur(int fakturId) {
     List<FakturPembelianItem> list = new java.util.ArrayList<>();
     String sql = "SELECT i.*, p.nama_produk, p.sku FROM faktur_items i JOIN products p ON i.produk_id = p.produk_id WHERE faktur_id = ?";
     try (Connection conn = config.DatabaseConnection.getConnection();
          PreparedStatement ps = conn.prepareStatement(sql)) {
          ps.setInt(1, fakturId);
          ResultSet rs = ps.executeQuery();
          while(rs.next()){
               FakturPembelianItem fi = new FakturPembelianItem();
               fi.setProdukID(rs.getInt("produk_id"));
               fi.setQty(rs.getInt("qty"));
               fi.setHargaBeliSatuan(rs.getDouble("harga_beli_satuan"));
               // Anda mungkin perlu nambah field namaProduk di model, atau cukup logic tampilkan ID sementara
               list.add(fi);
          }
     } catch(Exception e){}
     return list;
}

    private void closeResources(Connection conn, Statement... stmts) {
        try {
            if (stmts != null) for (Statement s : stmts) if (s != null) s.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {}
    }
}