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
import javax.servlet.ServletException;

@WebServlet("/product")
public class ProductServlet extends HttpServlet {
    ProductDAO dao = new ProductDAO();

    // MENANGANI LINK DELETE (GET REQUEST)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if(user == null || !(user instanceof Admin)) { resp.sendRedirect("index.jsp"); return; }
        
        String action = req.getParameter("action");
        if("delete".equals(action)) {
            String sku = req.getParameter("sku");
            if(dao.deleteProduct(sku)) {
                resp.sendRedirect("dashboard-admin.jsp?tab=produk&msg=deleted");
            } else {
                resp.sendRedirect("dashboard-admin.jsp?tab=produk&msg=fail_delete");
            }
        } else {
             // Jika bukan delete, kembalikan ke dashboard
             resp.sendRedirect("dashboard-admin.jsp");
        }
    }

    // MENANGANI FORM TAMBAH & UPDATE (POST REQUEST)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute("user");
        if(user == null || !(user instanceof Admin)) { resp.sendRedirect("index.jsp"); return; }

        String action = req.getParameter("action");
        
        // Ambil Data
        String sku = req.getParameter("sku");
        String nama = req.getParameter("nama");
        
        // Validasi dan Parsing
        double harga = 0;
        int stok = 0;
        int katID = 1;
        
        try {
            harga = Double.parseDouble(req.getParameter("harga"));
            // Ambil Parameter STOK yang baru ditambahkan
            stok = Integer.parseInt(req.getParameter("stok")); 
            katID = Integer.parseInt(req.getParameter("kategori_id"));
        } catch (Exception e) {
            resp.sendRedirect("dashboard-admin.jsp?tab=produk&msg=invalid_input");
            return;
        }

        Product p = new Product();
        p.setSku(sku); 
        p.setNamaProduk(nama); 
        p.setHargaJual(harga);
        p.setStok(stok); // Set stok ke model

        if("add".equals(action)) {
            if(dao.insertProduct(p, katID)){
                resp.sendRedirect("dashboard-admin.jsp?tab=produk&msg=saved");
            } else {
                resp.sendRedirect("dashboard-admin.jsp?tab=produk&msg=err_sku_duplicate");
            }
        } else if("update".equals(action)) {
            if(dao.updateProduct(p, katID)) {
                resp.sendRedirect("dashboard-admin.jsp?tab=produk&msg=updated");
            } else {
                resp.sendRedirect("dashboard-admin.jsp?tab=produk&msg=err_update");
            }
        }
    }
}