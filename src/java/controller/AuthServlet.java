/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import model.*;
import config.DatabaseConnection;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/auth"})
public class AuthServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        
        if ("login".equals(action)) {
            String u = req.getParameter("username");
            String p = req.getParameter("password");
            
            try (Connection conn = DatabaseConnection.getKoneksi()) {
                String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, u);
                ps.setString(2, p);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    String role = rs.getString("role");
                    int uid = rs.getInt("user_id");
                    
                    // Polimorfisme
                    User currentUser;
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        currentUser = new Admin(uid, u, true);
                    } else {
                        currentUser = new Kasir(uid, u, true);
                    }
                    
                    HttpSession session = req.getSession();
                    session.setAttribute("user", currentUser);
                    
                    if (currentUser instanceof Admin) {
                        resp.sendRedirect("admin_home");
                    } else {
                        resp.sendRedirect("pos");
                    } 
                } else {
                    resp.sendRedirect("index.jsp?err=1");
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else if("logout".equals(action)) {
            req.getSession().invalidate();
            resp.sendRedirect("index.jsp");
        }
    }
}