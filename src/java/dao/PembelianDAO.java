package dao;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import model.FakturPembelian;
import model.FakturPembelianItem;
import model.Product;
import config.DatabaseConnection;

public class PembelianDAO {

    private static final Logger LOGGER = Logger.getLogger(PembelianDAO.class.getName());

    // Method untuk mencari produk berdasarkan SKU/ID
    public Product cariProdukByScan(String keyword) {
        Product p = null;
        String sql = "SELECT * FROM products WHERE sku = ? OR produk_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, keyword);
            int idParam = -1;
            try { 
                idParam = Integer.parseInt(keyword); 
            } catch (NumberFormatException ignored) {}
            ps.setInt(2, idParam);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new Product();
                    p.setProdukID(rs.getInt("produk_id"));
                    p.setSku(rs.getString("sku"));
                    p.setNamaProduk(rs.getString("nama_produk"));
                    p.setHargaBeli(rs.getDouble("harga_beli"));
                    p.setHargaJual(rs.getDouble("harga_jual"));
                    p.setStok(rs.getInt("stok"));
                    try { p.setKategoriID(rs.getInt("kategori_id")); } catch (SQLException ignore) {}
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error cariProdukByScan: " + keyword, e);
        }
        return p;
    }

    // Method untuk Simpan Langsung (Admin Restock)
    public boolean simpanRestock(FakturPembelian faktur, int adminID) {
        Connection conn = null;

        String sqlFaktur = "INSERT INTO faktur_pembelian (no_faktur, waktu, status, admin_id_approver) VALUES (?, NOW(), 'APPROVED', ?)";
        String sqlItem = "INSERT INTO faktur_items (faktur_id, produk_id, qty, harga_beli_satuan) VALUES (?, ?, ?, ?)";
        String sqlProd = "UPDATE products SET stok = stok + ?, harga_beli = ? WHERE produk_id = ?";
        String sqlLog = "INSERT INTO stock_movements (produk_id, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, 'IN_PURCHASE', ?, ?, ?)";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); 

            // 1. Simpan Header
            int fakturId = 0;
            try (PreparedStatement psFaktur = conn.prepareStatement(sqlFaktur, Statement.RETURN_GENERATED_KEYS)) {
                psFaktur.setString(1, faktur.getNoFaktur());
                psFaktur.setInt(2, adminID);
                psFaktur.executeUpdate();

                try (ResultSet rs = psFaktur.getGeneratedKeys()) {
                    if (rs.next()) fakturId = rs.getInt(1);
                }
            }

            if (fakturId == 0) throw new SQLException("Gagal mendapatkan ID Faktur.");

            // 2. Loop Item & Update Stok (Multiple Resources TWR)
            try (PreparedStatement psItem = conn.prepareStatement(sqlItem);
                 PreparedStatement psProd = conn.prepareStatement(sqlProd);
                 PreparedStatement psLog = conn.prepareStatement(sqlLog)) {

                for (FakturPembelianItem item : faktur.getItems()) {
                    // a. Insert Item
                    psItem.setInt(1, fakturId);
                    psItem.setInt(2, item.getProdukID());
                    psItem.setInt(3, item.getQty());
                    psItem.setDouble(4, item.getHargaBeliSatuan());
                    psItem.executeUpdate();

                    // b. Update Master Product
                    psProd.setInt(1, item.getQty());
                    psProd.setDouble(2, item.getHargaBeliSatuan());
                    psProd.setInt(3, item.getProdukID());
                    psProd.executeUpdate();

                    // c. Log Mutasi
                    psLog.setInt(1, item.getProdukID());
                    psLog.setInt(2, item.getQty());
                    psLog.setString(3, String.valueOf(fakturId));
                    psLog.setInt(4, adminID);
                    psLog.executeUpdate();
                }
            }

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { 
                    conn.rollback(); 
                    LOGGER.log(Level.WARNING, "Transaksi Restock di-rollback.");
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Gagal Rollback", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Error simpanRestock", e);
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Gagal tutup koneksi", ex);
            }
        }
    }

    // KASIR: INPUT ITEM KE FAKTUR (DRAFT)
    public boolean inputItemRestockKasir(String noFaktur, String sku, int qty, double hargaBeli, int idKasir) {
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Cek Header DRAFT
            int fakturId = 0;
            String sqlCheck = "SELECT faktur_id FROM faktur_pembelian WHERE no_faktur = ? AND status = 'DRAFT'";
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, noFaktur);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        fakturId = rs.getInt("faktur_id");
                    }
                }
            }

            // Jika belum ada header, buat baru
            if (fakturId == 0) {
                String sqlHead = "INSERT INTO faktur_pembelian (no_faktur, waktu, status) VALUES (?, NOW(), 'DRAFT')";
                try (PreparedStatement psHead = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS)) {
                    psHead.setString(1, noFaktur);
                    psHead.executeUpdate();
                    try (ResultSet rsKey = psHead.getGeneratedKeys()) {
                        if (rsKey.next()) fakturId = rsKey.getInt(1);
                    }
                }
            }

            // 2. Cari ID Produk by SKU
            int produkId = 0;
            String sqlProd = "SELECT produk_id FROM products WHERE sku = ?";
            try (PreparedStatement psProd = conn.prepareStatement(sqlProd)) {
                psProd.setString(1, sku);
                try (ResultSet rsProd = psProd.executeQuery()) {
                    if (rsProd.next()) {
                        produkId = rsProd.getInt("produk_id");
                    } else {
                        conn.rollback();
                        return false; 
                    }
                }
            }

            // 3. Upsert Item
            String sqlCekItem = "SELECT id, qty FROM faktur_items WHERE faktur_id = ? AND produk_id = ?";
            try (PreparedStatement psCekItem = conn.prepareStatement(sqlCekItem)) {
                psCekItem.setInt(1, fakturId);
                psCekItem.setInt(2, produkId);

                boolean isUpdate;
                int currentId = 0;
                try (ResultSet rsItem = psCekItem.executeQuery()) {
                    isUpdate = rsItem.next();
                    if (isUpdate) currentId = rsItem.getInt("id");
                }
                
                if (isUpdate) {
                    try(PreparedStatement psUp = conn.prepareStatement("UPDATE faktur_items SET qty = qty + ?, harga_beli_satuan = ? WHERE id = ?")) {
                        psUp.setInt(1, qty);
                        psUp.setDouble(2, hargaBeli);
                        psUp.setInt(3, currentId);
                        psUp.executeUpdate();
                    }
                } else {
                    try(PreparedStatement psIn = conn.prepareStatement("INSERT INTO faktur_items (faktur_id, produk_id, qty, harga_beli_satuan) VALUES (?, ?, ?, ?)")) {
                        psIn.setInt(1, fakturId);
                        psIn.setInt(2, produkId);
                        psIn.setInt(3, qty);
                        psIn.setDouble(4, hargaBeli);
                        psIn.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            LOGGER.log(Level.SEVERE, "Error inputItemRestockKasir", e);
            return false;
        } finally {
            try { 
                if (conn != null) { conn.setAutoCommit(true); conn.close(); } 
            } catch (SQLException ex) {}
        }
    }
    
    public ResultSet getDraftInvoices() throws SQLException {
        String sql = "SELECT f.faktur_id, f.no_faktur, f.waktu, " +
                     "SUM(i.qty) as total_qty, " +
                     "COUNT(i.id) as jum_jenis, " +
                     "SUM(i.qty * i.harga_beli_satuan) as total_nilai " +
                     "FROM faktur_pembelian f " +
                     "LEFT JOIN faktur_items i ON f.faktur_id = i.faktur_id " +
                     "WHERE f.status = 'DRAFT' " +
                     "GROUP BY f.faktur_id ORDER BY f.waktu DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // FIX: Tambahkan suppress warnings "resource"
            // Alasan: Object 'crs' ini memang SENGAJA tidak ditutup (unclosed) 
            // di sini karena akan dikembalikan (return) ke caller (Servlet/JSP).
            @SuppressWarnings("resource")
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            
            crs.populate(rs);
            return crs; 
            
        } 
        // conn, stmt, dan rs asli otomatis tertutup aman di sini.
        // 'crs' tetap hidup berisi data copy dari rs.
    }

    // ADMIN: APPROVE FAKTUR
    public boolean approveFaktur(int fakturId, int adminId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlGet = "SELECT produk_id, qty, harga_beli_satuan FROM faktur_items WHERE faktur_id = ?";
            String sqlUp = "UPDATE products SET stok = stok + ?, harga_beli = ? WHERE produk_id = ?";
            String sqlMov = "INSERT INTO stock_movements (produk_id, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, 'IN_PURCHASE', ?, ?, ?)";
            
            try (PreparedStatement psGet = conn.prepareStatement(sqlGet);
                 PreparedStatement psUp = conn.prepareStatement(sqlUp);
                 PreparedStatement psLog = conn.prepareStatement(sqlMov)) {
                
                psGet.setInt(1, fakturId); 
                try (ResultSet rs = psGet.executeQuery()) {
                     while (rs.next()) {
                        int pid = rs.getInt("produk_id");
                        int qty = rs.getInt("qty");
                        double harga = rs.getDouble("harga_beli_satuan");

                        // Update Master
                        psUp.setInt(1, qty);
                        psUp.setDouble(2, harga);
                        psUp.setInt(3, pid);
                        psUp.executeUpdate();

                        // Log Movement
                        psLog.setInt(1, pid);
                        psLog.setInt(2, qty);
                        psLog.setString(3, String.valueOf(fakturId));
                        psLog.setInt(4, adminId);
                        psLog.executeUpdate();
                    }
                }
            }

            // Update Status Header
            String sqlStat = "UPDATE faktur_pembelian SET status = 'APPROVED', admin_id_approver = ? WHERE faktur_id = ?";
            try (PreparedStatement psStat = conn.prepareStatement(sqlStat)) {
                psStat.setInt(1, adminId);
                psStat.setInt(2, fakturId);
                psStat.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            LOGGER.log(Level.SEVERE, "Error approveFaktur", e);
            return false;
        } finally {
            try { 
                if (conn != null) { conn.setAutoCommit(true); conn.close(); } 
            } catch (SQLException ex) {}
        }
    }

    // ADMIN: REJECT FAKTUR
    public boolean rejectFaktur(int fakturId, int adminId) {
        String sql = "UPDATE faktur_pembelian SET status = 'REJECTED', admin_id_approver = ? WHERE faktur_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, adminId);
            ps.setInt(2, fakturId);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error rejectFaktur", e);
            return false;
        }
    }
}