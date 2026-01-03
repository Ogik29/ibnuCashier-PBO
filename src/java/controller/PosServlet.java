/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import model.*;
import connection.KoneksiDB;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/pos"})
public class PosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Produk> listProduk = new ArrayList<>();
        try (Connection conn = KoneksiDB.getKoneksi()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM products");
            while(rs.next()) {
                listProduk.add(new Produk(
                    rs.getInt("product_id"),
                    rs.getString("sku"),
                    rs.getString("nama_produk"),
                    rs.getDouble("harga_beli"),
                    rs.getDouble("harga_jual"),
                    rs.getInt("stok")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        req.setAttribute("listProduk", listProduk);
        req.getRequestDispatcher("pos.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String action = req.getParameter("action");
        
        List<SaleItem> keranjang = (List<SaleItem>) session.getAttribute("keranjang");
        if(keranjang == null) keranjang = new ArrayList<>();

        if ("tambah".equals(action)) {
            int pid = Integer.parseInt(req.getParameter("pid"));
            // ambil single product untuk membuat SaleItem
            try (Connection conn = KoneksiDB.getKoneksi()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM products WHERE product_id=?");
                ps.setInt(1, pid);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    Produk p = new Produk(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getDouble(5), rs.getInt(6));
                    
                    if(p.getStok() > 0) {
                        keranjang.add(new SaleItem(p, 1)); // Qty 1 default
                    }
                }
            } catch(Exception e){e.printStackTrace();}
            
            session.setAttribute("keranjang", keranjang);
            resp.sendRedirect("pos");

        } else if ("checkout".equals(action)) {
            User u = (User) session.getAttribute("user");
            
            try (Connection conn = KoneksiDB.getKoneksi()) {
                conn.setAutoCommit(false);
                
                // Logic Create Transaksi
                Transaksi trx = new Transaksi(); // Objek Model
                String noStruk = "INV-" + System.currentTimeMillis();
                double total = 0;
                for(SaleItem si : keranjang) total += si.getSubtotal();
                
                String sqlHead = "INSERT INTO transactions (no_struk, waktu, total, kasir_id, metode_bayar) VALUES (?, NOW(), ?, ?, 'CASH')";
                PreparedStatement psHead = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS);
                psHead.setString(1, noStruk);
                psHead.setDouble(2, total);
                psHead.setInt(3, u.getUserID());
                psHead.executeUpdate();
                
                int transId = 0;
                ResultSet gk = psHead.getGeneratedKeys();
                if(gk.next()) transId = gk.getInt(1);

                String sqlDet = "INSERT INTO sale_items (transaksi_id, produk_id, qty, subtotal) VALUES (?,?,?,?)";
                PreparedStatement psDet = conn.prepareStatement(sqlDet);
                
                String sqlUpd = "UPDATE products SET stok = stok - ? WHERE product_id = ?";
                PreparedStatement psUpd = conn.prepareStatement(sqlUpd);

                String sqlMov = "INSERT INTO stock_movements (produk_id, waktu, tipe_mutasi, qty_change, referensi_id, user_id) VALUES (?, NOW(), 'OUT', ?, ?, ?)";
                PreparedStatement psMov = conn.prepareStatement(sqlMov);

                for(SaleItem si : keranjang) {
                    // Insert Item
                    psDet.setInt(1, transId);
                    psDet.setInt(2, si.getProdukRef().getProdukID());
                    psDet.setInt(3, si.getQty());
                    psDet.setDouble(4, si.getSubtotal());
                    psDet.executeUpdate();
                    
                    // Update Stock
                    psUpd.setInt(1, si.getQty());
                    psUpd.setInt(2, si.getProdukRef().getProdukID());
                    psUpd.executeUpdate();
                    
                    // Create StockMovement (Model Call & Insert)
                    // Menggunakan Logic Model StockMovement.create() di memory, tapi save di sini
                    StockMovement sm = StockMovement.create(si.getProdukRef().getProdukID(), "OUT", -si.getQty(), noStruk, u.getUserID());
                    
                    psMov.setInt(1, si.getProdukRef().getProdukID());
                    psMov.setInt(2, -si.getQty()); // Negatif utk keluar
                    psMov.setString(3, noStruk);
                    psMov.setInt(4, u.getUserID());
                    psMov.executeUpdate();
                }

                conn.commit();
                session.removeAttribute("keranjang");
                
            } catch(Exception e) { e.printStackTrace(); }
            resp.sendRedirect("pos");
        } else if ("clear".equals(action)) {
            session.removeAttribute("keranjang");
            resp.sendRedirect("pos");
        }
    }
}