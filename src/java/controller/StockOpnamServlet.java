/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.*;
import model.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/opnam")
public class StockOpnamServlet extends HttpServlet {
    StockOpnamDAO dao = new StockOpnamDAO();
    ProductDAO pDao = new ProductDAO();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        String action = req.getParameter("action");

        if("create_draft".equals(action) && user instanceof Kasir) {
            // Logika Sederhana: Input ID dan Qty Fisik dipisah koma atau form loop
            // Ini contoh menerima input hidden utk simulasi
            String[] skus = req.getParameterValues("sku");
            String[] qtys = req.getParameterValues("qty");
            
            if(skus != null) {
                List<StockOpnamItem> items = new ArrayList<>();
                for(int i = 0; i < skus.length; i++) {
                    Product p = pDao.getBySku(skus[i]);
                    if(p != null) {
                        StockOpnamItem item = new StockOpnamItem();
                        item.setProdukID(p.getProdukID());
                        item.setQtyFisik(Integer.parseInt(qtys[i]));
                        item.setQtySistem(p.getStok()); // Kunci stok sistem saat ini
                        items.add(item);
                    }
                }
                dao.createDraft(user.getUserID(), items);
            }
            resp.sendRedirect("pos.jsp?msg=draft_created");

        } else if("approve_post".equals(action) && user instanceof Admin) {
            int opnamID = Integer.parseInt(req.getParameter("id"));
            // Admin flow: Approve, Lalu Post Changes
            boolean approved = dao.approve(opnamID, user.getUserID());
            if(approved) {
                dao.postOpnam(opnamID, user.getUserID());
                resp.sendRedirect("dashboard-admin.jsp?msg=posted");
            } else {
                resp.sendRedirect("dashboard-admin.jsp?msg=failed");
            }
        }
    }
}
