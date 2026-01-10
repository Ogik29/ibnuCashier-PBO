/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.CategoryDAO;
import model.Admin;
import model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/category")
public class CategoryServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(CategoryServlet.class.getName());
    private final CategoryDAO dao = new CategoryDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Ambil session
        HttpSession session = req.getSession(false); 
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        // Validasi: Hanya Admin yang boleh akses
        if(user == null || !(user instanceof Admin)) { 
            try {
                // FIX: Membungkus sendRedirect dalam try-catch untuk memenuhi aturan Sonar
                resp.sendRedirect("index.jsp"); 
            } catch (IOException e) {
                // Mencatat error tanpa menghentikan logic secara paksa
                LOGGER.log(Level.SEVERE, "Gagal melakukan redirect ke halaman login", e);
            }
            return; 
        }

        String action = req.getParameter("action");
        
        try {
            if("add".equals(action)) {
                String nama = req.getParameter("nama");
                if (nama != null && !nama.trim().isEmpty()) {
                    dao.add(nama);
                }
            } else if("delete".equals(action)) {
                String idParam = req.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    dao.delete(Integer.parseInt(idParam));
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format ID Kategori tidak valid saat operasi: " + action, e);
        }
        
        try {
            // FIX: Handle redirect akhir juga dengan try-catch
            resp.sendRedirect("dashboard-admin.jsp?tab=kategori");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Gagal redirect kembali ke dashboard admin", e);
        }
    }
}