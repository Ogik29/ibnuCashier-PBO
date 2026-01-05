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

@WebServlet("/product")
public class ProductServlet extends HttpServlet {
    ProductDAO dao = new ProductDAO();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute("user");
        if(user == null || !(user instanceof Admin)) { resp.sendRedirect("index.jsp"); return; }

        String action = req.getParameter("action");
        if("delete".equals(action)) {
            dao.deleteProduct(req.getParameter("sku"));
            resp.sendRedirect("dashboard-admin.jsp?tab=produk");
            return;
        }

        // Add / Update
        String sku = req.getParameter("sku");
        String nama = req.getParameter("nama");
        double harga = Double.parseDouble(req.getParameter("harga"));
        int katID = Integer.parseInt(req.getParameter("kategori_id")); // New Param

        Product p = new Product();
        p.setSku(sku); p.setNamaProduk(nama); p.setHargaJual(harga);

        if("add".equals(action)) {
            dao.insertProduct(p, katID);
        } else if("update".equals(action)) {
            dao.updateProduct(p, katID);
        }
        resp.sendRedirect("dashboard-admin.jsp?tab=produk");
    }
}