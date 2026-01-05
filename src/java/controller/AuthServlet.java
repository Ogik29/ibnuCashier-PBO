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

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    UserDAO userDAO = new UserDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if("logout".equals(action)) {
            req.getSession().invalidate();
            resp.sendRedirect("index.jsp");
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = userDAO.login(req.getParameter("username"), req.getParameter("password"));
        if(user != null) {
            req.getSession().setAttribute("user", user);
            if(user instanceof Admin) {
                resp.sendRedirect("dashboard-admin.jsp");
            } else {
                resp.sendRedirect("pos.jsp");
            }
        } else {
            resp.sendRedirect("index.jsp?error=invalid");
        }
    }
}