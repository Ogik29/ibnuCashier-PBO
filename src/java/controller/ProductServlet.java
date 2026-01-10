/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.ProductDAO;
import model.Admin;
import model.Product;
import model.User;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;

@WebServlet("/product")
public class ProductServlet extends HttpServlet {
    
    // 1. Inisialisasi Logger
    private static final Logger LOGGER = Logger.getLogger(ProductServlet.class.getName());

    // 2. Private final untuk keamanan thread
    private final ProductDAO dao = new ProductDAO();

    // MENANGANI LINK DELETE (GET REQUEST)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Ambil session dan cek login
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null || !(user instanceof Admin)) { 
            safeRedirect(resp, "index.jsp");
            return; 
        }
        
        String action = req.getParameter("action");
        if ("delete".equals(action)) {
            String sku = req.getParameter("sku");
            
            // Validasi SKU tidak boleh null
            if (sku != null && !sku.trim().isEmpty()) {
                if (dao.deleteProduct(sku)) {
                    safeRedirect(resp, "dashboard-admin.jsp?tab=produk&msg=deleted");
                } else {
                    safeRedirect(resp, "dashboard-admin.jsp?tab=produk&msg=fail_delete");
                }
            } else {
                 safeRedirect(resp, "dashboard-admin.jsp?tab=produk&msg=invalid_id");
            }
        } else {
             // Jika bukan delete, kembalikan ke dashboard
             safeRedirect(resp, "dashboard-admin.jsp");
        }
    }

    // MENANGANI FORM TAMBAH & UPDATE (POST REQUEST)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null || !(user instanceof Admin)) { 
            safeRedirect(resp, "index.jsp");
            return; 
        }

        String action = req.getParameter("action");
        
        // Ambil Data
        String sku = req.getParameter("sku");
        String nama = req.getParameter("nama");
        
        // Validasi dan Parsing
        double harga = 0;
        int stok = 0;
        int katID = 1;
        
        try {
            String hargaParam = req.getParameter("harga");
            String stokParam = req.getParameter("stok");
            String katParam = req.getParameter("kategori_id");
            
            // Cek null sebelum parsing
            if (hargaParam != null) harga = Double.parseDouble(hargaParam);
            if (stokParam != null) stok = Integer.parseInt(stokParam);
            if (katParam != null) katID = Integer.parseInt(katParam);
            
        } catch (NumberFormatException e) {
            safeRedirect(resp, "dashboard-admin.jsp?tab=produk&msg=invalid_input");
            return;
        }

        Product p = new Product();
        p.setSku(sku); 
        p.setNamaProduk(nama); 
        p.setHargaJual(harga);
        p.setStok(stok); 

        if ("add".equals(action)) {
            if (dao.insertProduct(p, katID)){
                safeRedirect(resp, "dashboard-admin.jsp?tab=produk&msg=saved");
            } else {
                safeRedirect(resp, "dashboard-admin.jsp?tab=produk&msg=err_sku_duplicate");
            }
        } else if ("update".equals(action)) {
            if (dao.updateProduct(p, katID)) {
                safeRedirect(resp, "dashboard-admin.jsp?tab=produk&msg=updated");
            } else {
                safeRedirect(resp, "dashboard-admin.jsp?tab=produk&msg=err_update");
            }
        } else {
            safeRedirect(resp, "dashboard-admin.jsp");
        }
    }

    /**
     * Helper Method untuk menangani "Handle exception" pada sendRedirect.
     * Mencegah duplikasi try-catch di logic utama.
     */
    private void safeRedirect(HttpServletResponse resp, String url) {
        try {
            resp.sendRedirect(url);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Gagal redirect ke: " + url, e);
        }
    }
}