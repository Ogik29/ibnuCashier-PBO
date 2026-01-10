/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.UserDAO;
import model.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    
    // 1. Inisialisasi Logger
    private static final Logger LOGGER = Logger.getLogger(AuthServlet.class.getName());

    // 2. Private final untuk keamanan thread (Thread-safe)
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("logout".equals(action)) {
            // Menghapus session saat logout
            req.getSession().invalidate(); 
            
            // FIX: Gunakan safeRedirect untuk menangani IOException
            safeRedirect(resp, "index.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = userDAO.login(req.getParameter("username"), req.getParameter("password"));

        if (user != null) {
            // Syarat: Pastikan class 'User' implements Serializable di package model
            req.getSession().setAttribute("user", user);

            // Cek tipe user & redirect aman
            if (user instanceof Admin) {
                safeRedirect(resp, "dashboard-admin.jsp");
            } else {
                safeRedirect(resp, "pos.jsp");
            }
        } else {
            safeRedirect(resp, "index.jsp?error=invalid");
        }
    }

    /**
     * Helper Method untuk menangani "Handle the following exception: IOException"
     * Membungkus logic sendRedirect dalam try-catch agar kode utama tetap bersih.
     */
    private void safeRedirect(HttpServletResponse resp, String location) {
        try {
            resp.sendRedirect(location);
        } catch (IOException e) {
            // Mencatat log error jika redirect gagal (misal koneksi putus)
            LOGGER.log(Level.SEVERE, "Gagal redirect ke halaman: " + location, e);
        }
    }
}