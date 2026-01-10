/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.ProductDAO;
import dao.StockOpnamDAO;
import model.Kasir;
import model.StockOpnamItem;
import model.User;
import model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/opnam")
public class StockOpnamServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(StockOpnamServlet.class.getName());
    
    private final StockOpnamDAO dao = new StockOpnamDAO();
    private final ProductDAO pDao = new ProductDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false); // Jangan buat session baru jika null
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        // Security check dasar
        if (user == null) {
            safeRedirect(resp, "index.jsp");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "";

        if ("create_draft".equals(action)) {
            if (!(user instanceof Kasir)) { 
                safeRedirect(resp, "index.jsp"); 
                return; 
            }
            
            try {
                String sku = req.getParameter("sku");
                // Validasi input angka
                int qty = Integer.parseInt(req.getParameter("qty"));
                
                Product p = pDao.getBySku(sku);
                
                if (p != null) {
                    List<StockOpnamItem> items = new ArrayList<>();
                    StockOpnamItem item = new StockOpnamItem();
                    item.setProdukID(p.getProdukID());
                    item.setQtyFisik(qty);
                    items.add(item);
                    
                    // Membuat draft stock opnam
                    dao.createDraft(user.getUserID(), items);
                    safeRedirect(resp, "pos.jsp?msg=opnam_ok");
                } else {
                    safeRedirect(resp, "pos.jsp?msg=opnam_fail_sku");
                }
            } catch (NumberFormatException e) {
                // Menangani jika qty bukan angka valid
                safeRedirect(resp, "pos.jsp?msg=invalid_input");
            } catch (Exception e) {
                // Mencatat log error sistem
                LOGGER.log(Level.SEVERE, "Error sistem saat create draft opnam", e);
                safeRedirect(resp, "pos.jsp?msg=error_system");
            }
        } 
        
        else if ("ADMIN".equals(user.getRole())) { 
            
            // Parameter ID dibutuhkan untuk approve/reject
            int opnamID = 0;
            try {
                String idParam = req.getParameter("id");
                if (idParam != null) {
                    opnamID = Integer.parseInt(idParam);
                }
            } catch (NumberFormatException e) {
                safeRedirect(resp, "dashboard-admin.jsp?tab=opnam&msg=invalid_id");
                return;
            }

            if ("approve".equals(action)) {
                 boolean success = dao.approveAndPost(opnamID, user.getUserID());
                 String msg = success ? "approved" : "fail";
                 safeRedirect(resp, "dashboard-admin.jsp?tab=opnam&msg=" + msg);
            } 
            else if ("reject".equals(action)) {
                 boolean success = dao.rejectOpnam(opnamID, user.getUserID());
                 String msg = success ? "rejected" : "fail";
                 safeRedirect(resp, "dashboard-admin.jsp?tab=opnam&msg=" + msg);
            } else {
                safeRedirect(resp, "dashboard-admin.jsp");
            }
        } else {
            // Jika user login tapi bukan ADMIN (misal mencoba hack URL)
            safeRedirect(resp, "index.jsp");
        }
    }

    private void safeRedirect(HttpServletResponse resp, String url) {
        try {
            resp.sendRedirect(url);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Gagal redirect ke: " + url, e);
        }
    }
}