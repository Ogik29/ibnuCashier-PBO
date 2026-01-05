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

@WebServlet("/opnam")
public class StockOpnamServlet extends HttpServlet {
    StockOpnamDAO dao = new StockOpnamDAO();
    ProductDAO pDao = new ProductDAO();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        String action = req.getParameter("action");

        // CREATE DRAFT (Kasir)
        if("create_draft".equals(action)) {
            if(!(user instanceof Kasir)) { resp.sendRedirect("index.jsp"); return; }
            
            String sku = req.getParameter("sku");
            int qty = Integer.parseInt(req.getParameter("qty"));
            
            Product p = pDao.getBySku(sku);
            if(p != null) {
                List<StockOpnamItem> items = new ArrayList<>();
                StockOpnamItem item = new StockOpnamItem();
                item.setProdukID(p.getProdukID());
                item.setQtyFisik(qty);
                items.add(item);
                
                dao.createDraft(user.getUserID(), items);
                resp.sendRedirect("pos.jsp?msg=opnam_ok");
            } else {
                resp.sendRedirect("pos.jsp?msg=opnam_fail");
            }
        } 
        
        // ADMIN ACTIONS
        else if(user != null && "ADMIN".equals(user.getRole())) {
             int opnamID = Integer.parseInt(req.getParameter("id"));

             if("approve".equals(action)) {
                 boolean success = dao.approveAndPost(opnamID, user.getUserID());
                 resp.sendRedirect("dashboard-admin.jsp?tab=opnam&msg=" + (success ? "approved" : "fail"));
             } 
             
             else if("reject".equals(action)) {
                 boolean success = dao.rejectOpnam(opnamID, user.getUserID());
                 resp.sendRedirect("dashboard-admin.jsp?tab=opnam&msg=" + (success ? "rejected" : "fail"));
             }
        }
    }
}