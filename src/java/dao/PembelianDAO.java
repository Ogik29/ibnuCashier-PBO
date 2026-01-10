package dao;

import java.sql.*;
import model.FakturPembelian;
import model.FakturPembelianItem;
import model.Product;
import config.DatabaseConnection;

public class PembelianDAO {


    // Method untuk mencari produk berdasarkan SKU/ID (Dipanggil oleh ProsesTransaksiServlet)
    public Product cariProdukByScan(String keyword) {
        Product p = null;
        // Cari by SKU atau ID
        String sql = "SELECT * FROM products WHERE sku = ? OR produk_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, keyword);
            // Cek apakah keyword angka untuk parameter kedua (ID)
            try { 
                ps.setInt(2, Integer.parseInt(keyword)); 
            } catch (NumberFormatException e) { 
                ps.setInt(2, -1); 
            }
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                p = new Product();
                p.setProdukID(rs.getInt("produk_id"));
                p.setSku(rs.getString("sku"));
                p.setNamaProduk(rs.getString("nama_produk"));
                p.setHargaBeli(rs.getDouble("harga_beli"));
                p.setHargaJual(rs.getDouble("harga_jual"));
                p.setStok(rs.getInt("stok"));
                // Tambahan mapping kategori jika perlu
                p.setKategoriID(rs.getInt("kategori_id")); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    // Method untuk Simpan Langsung (Admin Restock via ProsesTransaksiServlet)
    public boolean simpanRestock(FakturPembelian faktur, int adminID) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaksi

            // 1. Simpan Header (Langsung APPROVED karena Admin yang input)
            String sqlFaktur = "INSERT INTO faktur_pembelian (no_faktur, waktu, status, admin_id_approver) VALUES (?, NOW(), 'APPROVED', ?)";
            PreparedStatement psFaktur = conn.prepareStatement(sqlFaktur, Statement.RETURN_GENERATED_KEYS);
            psFaktur.setString(1, faktur.getNoFaktur());
            psFaktur.setInt(2, adminID);
            psFaktur.executeUpdate();

            // Get ID Faktur
            int fakturId = 0;
            ResultSet rs = psFaktur.getGeneratedKeys();
            if (rs.next()) fakturId = rs.getInt(1);

            // 2. Simpan Item & Update Stok
            for (FakturPembelianItem item : faktur.getItems()) {
                // Insert Item
                String sqlItem = "INSERT INTO faktur_items (faktur_id, produk_id, qty, harga_beli_satuan) VALUES (?, ?, ?, ?)";
                PreparedStatement psItem = conn.prepareStatement(sqlItem);
                psItem.setInt(1, fakturId);
                psItem.setInt(2, item.getProdukID());
                psItem.setInt(3, item.getQty());
                psItem.setDouble(4, item.getHargaBeliSatuan());
                psItem.executeUpdate();

                // Update Master Product (Stok + Harga)
                String sqlProd = "UPDATE products SET stok = stok + ?, harga_beli = ? WHERE produk_id = ?";
                PreparedStatement psProd = conn.prepareStatement(sqlProd);
                psProd.setInt(1, item.getQty());
                psProd.setDouble(2, item.getHargaBeliSatuan());
                psProd.setInt(3, item.getProdukID());
                psProd.executeUpdate();
                
                // Log Mutasi
                String sqlLog = "INSERT INTO stock_movements (produk_id, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, 'IN_PURCHASE', ?, ?, ?)";
                PreparedStatement psLog = conn.prepareStatement(sqlLog);
                psLog.setInt(1, item.getProdukID());
                psLog.setInt(2, item.getQty());
                psLog.setString(3, String.valueOf(fakturId));
                psLog.setInt(4, adminID);
                psLog.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ex) {}
        }
    }


    // =========================================================================
    // BAGIAN 2: DIGUNAKAN OLEH POS KASIR (INPUT DRAFT) & DASHBOARD ADMIN
    // =========================================================================

    // KASIR: INPUT ITEM KE FAKTUR (DRAFT)
    public boolean inputItemRestockKasir(String noFaktur, String sku, int qty, double hargaBeli, int idKasir) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Cek Header DRAFT
            int fakturId = 0;
            String sqlCheck = "SELECT faktur_id FROM faktur_pembelian WHERE no_faktur = ? AND status = 'DRAFT'";
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setString(1, noFaktur);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                fakturId = rs.getInt("faktur_id");
            } else {
                String sqlHead = "INSERT INTO faktur_pembelian (no_faktur, waktu, status) VALUES (?, NOW(), 'DRAFT')";
                PreparedStatement psHead = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS);
                psHead.setString(1, noFaktur);
                psHead.executeUpdate();
                ResultSet rsKey = psHead.getGeneratedKeys();
                if (rsKey.next()) fakturId = rsKey.getInt(1);
            }

            // Cari ID Produk
            int produkId = 0;
            PreparedStatement psProd = conn.prepareStatement("SELECT produk_id FROM products WHERE sku = ?");
            psProd.setString(1, sku);
            ResultSet rsProd = psProd.executeQuery();
            if (rsProd.next()) {
                produkId = rsProd.getInt("produk_id");
            } else {
                return false; 
            }

            // Upsert Item (Update jika ada, Insert jika baru)
            String sqlCekItem = "SELECT id, qty FROM faktur_items WHERE faktur_id = ? AND produk_id = ?";
            PreparedStatement psCekItem = conn.prepareStatement(sqlCekItem);
            psCekItem.setInt(1, fakturId);
            psCekItem.setInt(2, produkId);
            ResultSet rsItem = psCekItem.executeQuery();

            if (rsItem.next()) {
                // Update
                String sqlUpdate = "UPDATE faktur_items SET qty = qty + ?, harga_beli_satuan = ? WHERE id = ?";
                PreparedStatement psUp = conn.prepareStatement(sqlUpdate);
                psUp.setInt(1, qty);
                psUp.setDouble(2, hargaBeli);
                psUp.setInt(3, rsItem.getInt("id"));
                psUp.executeUpdate();
            } else {
                // Insert
                String sqlItem = "INSERT INTO faktur_items (faktur_id, produk_id, qty, harga_beli_satuan) VALUES (?, ?, ?, ?)";
                PreparedStatement psItem = conn.prepareStatement(sqlItem);
                psItem.setInt(1, fakturId);
                psItem.setInt(2, produkId);
                psItem.setInt(3, qty);
                psItem.setDouble(4, hargaBeli);
                psItem.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ex) {}
        }
    }

    // ADMIN: LIST DRAFT FAKTUR (SUM QTY)
    public ResultSet getDraftInvoices() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();
        String sql = "SELECT f.faktur_id, f.no_faktur, f.waktu, " +
                     "SUM(i.qty) as total_qty, " +
                     "COUNT(i.id) as jum_jenis, " +
                     "SUM(i.qty * i.harga_beli_satuan) as total_nilai " +
                     "FROM faktur_pembelian f " +
                     "LEFT JOIN faktur_items i ON f.faktur_id = i.faktur_id " +
                     "WHERE f.status = 'DRAFT' " +
                     "GROUP BY f.faktur_id ORDER BY f.waktu DESC";
        return stmt.executeQuery(sql);
    }

    // ADMIN: APPROVE FAKTUR (Finalisasi Stok)
    public boolean approveFaktur(int fakturId, int adminId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Loop items di faktur
            String sqlGet = "SELECT produk_id, qty, harga_beli_satuan FROM faktur_items WHERE faktur_id = ?";
            PreparedStatement psGet = conn.prepareStatement(sqlGet);
            psGet.setInt(1, fakturId);
            ResultSet rs = psGet.executeQuery();

            while (rs.next()) {
                int pid = rs.getInt("produk_id");
                int qty = rs.getInt("qty");
                double harga = rs.getDouble("harga_beli_satuan");

                // Update Master
                String sqlUp = "UPDATE products SET stok = stok + ?, harga_beli = ? WHERE produk_id = ?";
                PreparedStatement psUp = conn.prepareStatement(sqlUp);
                psUp.setInt(1, qty);
                psUp.setDouble(2, harga);
                psUp.setInt(3, pid);
                psUp.executeUpdate();

                // Log Movement
                String sqlMov = "INSERT INTO stock_movements (produk_id, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, 'IN_PURCHASE', ?, ?, ?)";
                PreparedStatement psL = conn.prepareStatement(sqlMov);
                psL.setInt(1, pid);
                psL.setInt(2, qty);
                psL.setString(3, String.valueOf(fakturId));
                psL.setInt(4, adminId);
                psL.executeUpdate();
            }

            // Update Status
            String sqlStat = "UPDATE faktur_pembelian SET status = 'APPROVED', admin_id_approver = ? WHERE faktur_id = ?";
            PreparedStatement psStat = conn.prepareStatement(sqlStat);
            psStat.setInt(1, adminId);
            psStat.setInt(2, fakturId);
            psStat.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ex) {}
        }
    }

    // ADMIN: REJECT FAKTUR
    public boolean rejectFaktur(int fakturId, int adminId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE faktur_pembelian SET status = 'REJECTED', admin_id_approver = ? WHERE faktur_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, adminId);
            ps.setInt(2, fakturId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}