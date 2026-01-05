<%-- 
    Document   : dashboard-admin
    Created on : 5 Jan 2026, 15.10.28
    Author     : dimas
--%>

<%@ page import="dao.*, model.*, java.util.*, java.sql.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Security Check
    User user = (User) session.getAttribute("user");
    if(user == null || !(user instanceof Admin)) {
        response.sendRedirect("index.jsp");
        return;
    }
    ProductDAO pDao = new ProductDAO();
    StockOpnamDAO oDao = new StockOpnamDAO();
    List<Product> products = pDao.getAllProducts();
    
    // Helper to get opnames pending
    ResultSet rsOpnam = oDao.getPendingOpnames(); 
%>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard - IBNU Cashier</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark bg-dark px-4">
    <span class="navbar-brand mb-0 h1">Admin Panel: <%= user.getUsername() %></span>
    <a href="auth?action=logout" class="btn btn-danger btn-sm">Logout</a>
</nav>

<div class="container mt-4">
    <!-- TABS -->
    <ul class="nav nav-tabs" id="myTab" role="tablist">
        <li class="nav-item"><a class="nav-link active" data-bs-toggle="tab" href="#produk">Produk</a></li>
        <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#opnam">Stok Opnam (Pending)</a></li>
    </ul>

    <div class="tab-content p-3 border border-top-0">
        <!-- TAB PRODUK -->
        <div class="tab-pane fade show active" id="produk">
            <h5>Kelola Produk</h5>
            
            <!-- Form Tambah -->
            <form action="product" method="post" class="row g-2 mb-4 bg-light p-2 rounded">
                <input type="hidden" name="action" value="add">
                <div class="col-md-3"><input type="text" name="sku" class="form-control" placeholder="SKU" required></div>
                <div class="col-md-4"><input type="text" name="nama" class="form-control" placeholder="Nama Produk" required></div>
                <div class="col-md-3"><input type="number" name="harga" class="form-control" placeholder="Harga Jual" required></div>
                <div class="col-md-2"><button type="submit" class="btn btn-success w-100">Tambah</button></div>
            </form>

            <table class="table table-bordered table-striped">
                <thead><tr><th>SKU</th><th>Nama</th><th>Stok</th><th>Harga</th><th>Action</th></tr></thead>
                <tbody>
                <% for(Product p : products) { %>
                    <tr>
                        <td><%= p.getSKU() %></td>
                        <td><%= p.getNamaProduk() %></td>
                        <td><%= p.getStok() %></td>
                        <td>Rp <%= p.getHargaJual() %></td>
                        <td>
                            <!-- Form Simple Update Harga -->
                            <form action="product" method="post" class="d-flex">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="sku" value="<%= p.getSKU() %>">
                                <input type="number" name="harga" value="<%= (int)p.getHargaJual() %>" class="form-control form-control-sm me-2" style="width:100px;">
                                <button type="submit" class="btn btn-warning btn-sm">Update</button>
                            </form>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>

        <!-- TAB OPNAM -->
        <div class="tab-pane fade" id="opnam">
            <h5>Pending Opnam Approvals</h5>
            <table class="table">
                <thead><tr><th>ID</th><th>Creator</th><th>Waktu</th><th>Action</th></tr></thead>
                <tbody>
                <% while(rsOpnam.next()) { %>
                    <tr>
                        <td><%= rsOpnam.getInt("opnam_id") %></td>
                        <td><%= rsOpnam.getString("creator") %></td>
                        <td><%= rsOpnam.getTimestamp("waktu") %></td>
                        <td>
                            <form action="opnam" method="post">
                                <input type="hidden" name="action" value="approve_post">
                                <input type="hidden" name="id" value="<%= rsOpnam.getInt("opnam_id") %>">
                                <button type="submit" class="btn btn-primary btn-sm">Review & Approve</button>
                            </form>
                        </td>
                    </tr>
                <% } rsOpnam.close(); %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
