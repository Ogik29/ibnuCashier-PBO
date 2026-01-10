/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.ProductDAO;
import dao.TransactionDAO;
import dao.PembelianDAO;
import model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/PosServlet")
public class PosServlet extends HttpServlet {
    
    // 1. Logger untuk mencatat error redirect
    private static final Logger LOGGER = Logger.getLogger(PosServlet.class.getName());
    
    // FIX 1: Jadikan 'private final' agar Thread-Safe (aman untuk banyak user)
    private final ProductDAO productDAO = new ProductDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final PembelianDAO pembelianDAO = new PembelianDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession();
        
        // 1. Validasi User Login (Keamanan)
        User user = (User) session.getAttribute("user");
        if (user == null || !(user instanceof Kasir)) {
            safeRedirect(resp, "index.jsp");
            return;
        }
        
        // Casting ke tipe Kasir
        Kasir kasir = (Kasir) user;

        // 2. Ambil Keranjang
        List<SaleItem> cart = (List<SaleItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        // === LOGIKA ADD ITEM KE KERANJANG ===
        if ("add".equals(action)) {
            String sku = req.getParameter("sku");
            
            // Ambil Qty dari input manual
            int qtyInput = 1;
            try {
                qtyInput = Integer.parseInt(req.getParameter("qty"));
            } catch (NumberFormatException e) {
                qtyInput = 1; 
            }

            Product p = productDAO.getBySku(sku);

            if (p != null) {
                // Cek stok awal di database
                if (p.getStok() <= 0) {
                     safeRedirect(resp, "pos.jsp?msg=failed");
                     return;
                }

                boolean exist = false;
                for (SaleItem item : cart) {
                    if (item.getProdukID() == p.getProdukID()) {
                        // Cek apakah stok cukup jika ditambah qty baru
                        if(item.getQty() + qtyInput > p.getStok()) {
                            // FIX: Menggunakan safeRedirect agar try-catch terpusat
                            safeRedirect(resp, "pos.jsp?error=Stok%20tidak%20cukup");
                            return;
                        }
                        
                        // Update Qty
                        item.setQty(item.getQty() + qtyInput);
                        exist = true;
                        break;
                    }
                }
                
                // Jika barang belum ada di keranjang
                if (!exist) {
                    if (qtyInput > p.getStok()) {
                        safeRedirect(resp, "pos.jsp?error=Stok%20tidak%20cukup");
                        return;
                    }
                    // Tambah item baru
                    cart.add(new SaleItem(p, qtyInput));
                }
            }
            // Simpan update ke session
            session.setAttribute("cart", cart);
            safeRedirect(resp, "pos.jsp");
        } 
        
        // === LOGIKA RESET KERANJANG ===
        else if("reset".equals(action)) {
            session.removeAttribute("cart");
            safeRedirect(resp, "pos.jsp");
        }
        
        // === LOGIKA CHECKOUT (TRANSAKSI) ===
        else if ("checkout".equals(action)) {
            if(cart.isEmpty()) {
                safeRedirect(resp, "pos.jsp?msg=empty");
                return;
            }

            Transaction trx = kasir.buatTransaksi(cart);
            trx.setKasirID(kasir.getUserID());
            
            if (transactionDAO.simpanTransaksi(trx)) {
                session.removeAttribute("cart"); 
                safeRedirect(resp, "pos.jsp?msg=success");
            } else {
                safeRedirect(resp, "pos.jsp?msg=failed");
            }
        }
        
        // === LOGIKA RESTOCK (INPUT FAKTUR PEMBELIAN DARI KASIR) ===
        else if ("input_restock_kasir".equals(action)) {
            String noFaktur = req.getParameter("noFaktur");
            String sku = req.getParameter("sku");
            int qty = 0;
            double harga = 0;

            try {
                qty = Integer.parseInt(req.getParameter("qty"));
                harga = Double.parseDouble(req.getParameter("hargaBeli"));
            } catch (NumberFormatException e) {
                safeRedirect(resp, "pos.jsp?msg=restock_fail");
                return;
            }
            
            if(pembelianDAO.inputItemRestockKasir(noFaktur, sku, qty, harga, user.getUserID())) {
                 safeRedirect(resp, "pos.jsp?msg=restock_ok");
            } else {
                 safeRedirect(resp, "pos.jsp?msg=restock_fail");
            }
        }
    }

    /**
     * Helper method untuk menangani Exception IOException saat Redirect
     * Method ini memuaskan requirement "Handle exception" tanpa mengotori logic utama
     */
    private void safeRedirect(HttpServletResponse resp, String url) {
        try {
            resp.sendRedirect(url);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Gagal redirect ke: " + url, e);
        }
    }
}