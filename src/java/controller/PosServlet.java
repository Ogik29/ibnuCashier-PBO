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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/PosServlet")
public class PosServlet extends HttpServlet {
    // Inisialisasi DAO
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
        // Casting ke tipe Kasir
        Kasir kasir = (Kasir) user;

        // 2. Ambil Keranjang
        List<SaleItem> cart = (List<SaleItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        // === LOGIKA ADD ITEM KE KERANJANG ===
        if ("add".equals(action)) {
            String sku = req.getParameter("sku");
            
            // Perbaikan: Ambil Qty dari input manual di pos.jsp
            int qtyInput = 1;
            try {
                qtyInput = Integer.parseInt(req.getParameter("qty"));
            } catch (Exception e) {
                qtyInput = 1; // Default jika null/error
            }

            Product p = productDAO.getBySku(sku);

            if (p != null) {
                // Cek stok awal
                if (p.getStok() <= 0) {
                     resp.sendRedirect("pos.jsp?msg=failed");
                     return;
                }

                boolean exist = false;
                for (SaleItem item : cart) {
                    if (item.getProdukID() == p.getProdukID()) {
                        // Cek apakah (qty saat ini + qty baru) melebihi stok?
                        if(item.getQty() + qtyInput > p.getStok()) {
                            resp.sendRedirect("pos.jsp?error=Stok%20tidak%20cukup");
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
                        resp.sendRedirect("pos.jsp?error=Stok%20tidak%20cukup");
                        return;
                    }
                    cart.add(new SaleItem(p, qtyInput));
                }
            }
            session.setAttribute("cart", cart);
            resp.sendRedirect("pos.jsp");
        } 
        
        // === LOGIKA RESET KERANJANG ===
        else if("reset".equals(action)) {
            session.removeAttribute("cart");
            resp.sendRedirect("pos.jsp");
        }
        
        // === LOGIKA CHECKOUT (TRANSAKSI) ===
        else if ("checkout".equals(action)) {
            if(cart.isEmpty()) {
                resp.sendRedirect("pos.jsp?msg=empty");
                return;
            }

            Transaction trx = kasir.buatTransaksi(cart);
            trx.setKasirID(kasir.getUserID());
            
            if (transactionDAO.simpanTransaksi(trx)) {
                session.removeAttribute("cart"); 
                resp.sendRedirect("pos.jsp?msg=success");
            } else {
                resp.sendRedirect("pos.jsp?msg=failed");
            }
        }
        
        // === LOGIKA RESTOCK (INPUT FAKTUR PEMBELIAN DARI KASIR) ===
        else if ("input_restock_kasir".equals(action)) {
            // Perbaikan: Gunakan 'req' bukan 'request'
            String noFaktur = req.getParameter("noFaktur");
            String sku = req.getParameter("sku");
            int qty = 0;
            double harga = 0;

            try {
                qty = Integer.parseInt(req.getParameter("qty"));
                harga = Double.parseDouble(req.getParameter("hargaBeli"));
            } catch (NumberFormatException e) {
                resp.sendRedirect("pos.jsp?msg=restock_fail");
                return;
            }

            PembelianDAO dao = new PembelianDAO();
            // Perbaikan: gunakan user.getUserID() dan 'resp' bukan 'response'
            if(dao.inputItemRestockKasir(noFaktur, sku, qty, harga, user.getUserID())) {
                 resp.sendRedirect("pos.jsp?msg=restock_ok");
            } else {
                 resp.sendRedirect("pos.jsp?msg=restock_fail");
            }
        }
    }
}