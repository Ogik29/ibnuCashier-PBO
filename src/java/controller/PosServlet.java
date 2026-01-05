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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession();
        
        // 1. Validasi User Login (Keamanan)
        User user = (User) session.getAttribute("user");
        if (user == null || !(user instanceof Kasir)) {
            resp.sendRedirect("index.jsp");
            return;
        }
        // Casting
        Kasir kasir = (Kasir) user;

        // 2. Ambil Keranjang
        List<SaleItem> cart = (List<SaleItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        // === LOGIKA BARU ADD ITEM (Dari List Produk) ===
        if ("add".equals(action)) {
            String sku = req.getParameter("sku");
            Product p = productDAO.getBySku(sku);

            if (p != null) {
                // Cek apakah stok habis sebelum masuk keranjang
                if (p.getStok() <= 0) {
                     resp.sendRedirect("pos.jsp?msg=failed");
                     return;
                }

                // Cek item sudah ada di keranjang?
                boolean exist = false;
                for (SaleItem item : cart) {
                    if (item.getProdukID() == p.getProdukID()) {
                        // Cek lagi apakah tambah qty melebihi stok db?
                        if(item.getQty() + 1 > p.getStok()) {
                            // Stok tidak cukup untuk nambah lagi
                            resp.sendRedirect("pos.jsp?msg=failed");
                            return;
                        }
                        
                        item.setQty(item.getQty() + 1);
                        exist = true;
                        break;
                    }
                }
                // Jika belum ada, tambah baru
                if (!exist) {
                    cart.add(new SaleItem(p, 1));
                }
            }
            session.setAttribute("cart", cart);
            resp.sendRedirect("pos.jsp");
            
        } 
        // === RESET KERANJANG ===
        else if("reset".equals(action)) {
            session.removeAttribute("cart");
            resp.sendRedirect("pos.jsp");
        }
        // === CHECKOUT (TRANSAKSI) ===
        else if ("checkout".equals(action)) {
            if(cart.isEmpty()) {
                resp.sendRedirect("pos.jsp?msg=empty");
                return;
            }

            Transaction trx = kasir.buatTransaksi(cart);
            trx.setKasirID(kasir.getUserID());
            
            // Simpan ke DB
            if (transactionDAO.simpanTransaksi(trx)) {
                session.removeAttribute("cart"); // Kosongkan cart jika sukses
                resp.sendRedirect("pos.jsp?msg=success");
            } else {
                resp.sendRedirect("pos.jsp?msg=failed");
            }
        }
    }
}