/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.ProductDAO;
import model.Admin;
import model.Product;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/product")
public class ProductServlet extends HttpServlet {
    ProductDAO dao = new ProductDAO();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Cek Admin
        if(!(req.getSession().getAttribute("user") instanceof Admin)) { resp.sendRedirect("index.jsp"); return; }
        
        String action = req.getParameter("action");
        
        if("add".equals(action)) {
            String sku = req.getParameter("sku");
            String nama = req.getParameter("nama");
            double harga = Double.parseDouble(req.getParameter("harga"));
            
            Product p = new Product(); 
            p.setSku(sku); p.setNamaProduk(nama); p.setHargaJual(harga);
            
            dao.insertProduct(p);
            resp.sendRedirect("dashboard-admin.jsp?tab=produk"); // Asumsi single page dashboard
            
        } else if("update".equals(action)) {
            // Logic update harga
            String sku = req.getParameter("sku");
            double hargaBaru = Double.parseDouble(req.getParameter("harga"));
            // Ambil produk dulu
            Product p = dao.getBySku(sku);
            if(p != null) {
                p.setHargaJual(hargaBaru);
                dao.updateProduct(p);
            }
            resp.sendRedirect("dashboard-admin.jsp?tab=produk");
        }
    }
}
