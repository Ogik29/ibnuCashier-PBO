/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.CategoryDAO;
import model.Admin;
import model.User;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/category")
public class CategoryServlet extends HttpServlet {
    CategoryDAO dao = new CategoryDAO();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute("user");
        if(user == null || !(user instanceof Admin)) { resp.sendRedirect("index.jsp"); return; }

        String action = req.getParameter("action");
        if("add".equals(action)) {
            dao.add(req.getParameter("nama"));
        } else if("delete".equals(action)) {
            dao.delete(Integer.parseInt(req.getParameter("id")));
        }
        resp.sendRedirect("dashboard-admin.jsp?tab=kategori");
    }
}