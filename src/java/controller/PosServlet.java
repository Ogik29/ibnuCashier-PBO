/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.ProductDAO;
import dao.TransactionDAO;
import model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/PosServlet")
public class PosServlet extends HttpServlet {
    ProductDAO productDAO = new ProductDAO();
    TransactionDAO transactionDAO = new TransactionDAO();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Kasir kasir = (Kasir) session.getAttribute("user"); // Casting ke Kasir sesuai diagram
        
        if(kasir == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        // Setup keranjang di session
        List<SaleItem> cart = (List<SaleItem>) session.getAttribute("cart");
        if(cart == null) cart = new ArrayList<>();

        String action = req.getParameter("action");

        if("add".equals(action)) {
            String sku = req.getParameter("sku");
            Product p = productDAO.getBySku(sku);
            if(p != null) {
                // Logika nambah item sederhana
                cart.add(new SaleItem(p, 1));
            }
            session.setAttribute("cart", cart);
            resp.sendRedirect("pos.jsp");
        
        } else if("checkout".equals(action)) {
            // Gunakan method 'buatTransaksi'
            Transaction trx = kasir.buatTransaksi(cart);
            trx.setKasirID(kasir.getUserID());
            
            // Simpan via DAO
            boolean success = transactionDAO.simpanTransaksi(trx);
            
            if(success) {
                session.removeAttribute("cart");
                resp.sendRedirect("pos.jsp?msg=success");
            } else {
                resp.sendRedirect("pos.jsp?msg=failed");
            }
        }
    }
}