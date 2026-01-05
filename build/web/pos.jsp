<%-- 
    Document   : pos
    Created on : 14 Dec 2025, 15.20.26
    Author     : dimas
--%>

<%@ page import="model.*, java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User user = (User) session.getAttribute("user");
    if(user == null || !(user instanceof Kasir)) {
        response.sendRedirect("index.jsp"); return;
    }
    List<SaleItem> cart = (List<SaleItem>) session.getAttribute("cart");
%>
<!DOCTYPE html>
<html>
<head>
    <title>KASIR POS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark bg-primary px-4">
    <span class="navbar-brand">KASIR: <%= user.getUsername() %></span>
    <div>
        <button class="btn btn-light btn-sm me-2" data-bs-toggle="modal" data-bs-target="#opnamModal">Buat Opnam</button>
        <a href="auth?action=logout" class="btn btn-danger btn-sm">Logout</a>
    </div>
</nav>

<div class="container mt-4 row">
    <!-- Kiri: Form Scan -->
    <div class="col-md-6">
        <div class="card p-4">
            <h4>Scan Produk</h4>
            <form action="PosServlet" method="post" autofocus>
                <input type="hidden" name="action" value="scan">
                <input type="text" name="sku" class="form-control form-control-lg mb-3" placeholder="Scan SKU Disini..." autofocus>
                <button type="submit" class="btn btn-primary w-100">Tambahkan ke Keranjang</button>
            </form>
            
            <% if(request.getParameter("msg")!=null && request.getParameter("msg").equals("success")) { %>
                <div class="alert alert-success mt-3">Transaksi Berhasil Disimpan!</div>
            <% } %>
        </div>
    </div>

    <!-- Kanan: Keranjang -->
    <div class="col-md-6">
        <div class="card p-3">
            <h4>Keranjang Belanja</h4>
            <table class="table">
                <thead><tr><th>Nama</th><th>Qty</th><th>Harga</th><th>Subtotal</th></tr></thead>
                <tbody>
                <% 
                    double grandTotal = 0;
                    if(cart != null) {
                        for(SaleItem item : cart) { 
                            grandTotal += item.getSubtotal();
                %>
                        <tr>
                            <td><%= item.getProduct().getNamaProduk() %></td>
                            <td><%= item.getQty() %></td>
                            <td><%= item.getHargaSatuan() %></td>
                            <td><%= item.getSubtotal() %></td>
                        </tr>
                <%      } 
                    } %>
                </tbody>
            </table>
            <h3 class="text-end text-danger">Total: Rp <%= (int)grandTotal %></h3>
            
            <form action="PosServlet" method="post" class="mt-3">
                <input type="hidden" name="action" value="checkout">
                <button class="btn btn-success btn-lg w-100" <%= (cart==null||cart.isEmpty()) ? "disabled" : "" %>>BAYAR (CHECKOUT)</button>
            </form>
        </div>
    </div>
</div>

<!-- Modal Simple Stock Opnam Draft -->
<div class="modal fade" id="opnamModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header"><h5 class="modal-title">Buat Draft Opnam</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
            <form action="opnam" method="post">
                <div class="modal-body">
                    <input type="hidden" name="action" value="create_draft">
                    <div class="alert alert-info">Masukkan SKU dan Jumlah Fisik (Manual Entry untuk Demo)</div>
                    <!-- Simulasi 1 item dulu untuk demo -->
                    <div class="input-group mb-2">
                        <input type="text" name="sku" placeholder="SKU Barang" class="form-control" required>
                        <input type="number" name="qty" placeholder="Fisik" class="form-control" required>
                    </div>
                     <!-- Bisa ditambah javascript dynamic form field -->
                </div>
                <div class="modal-footer">
                    <button type="submit" class="btn btn-warning">Kirim ke Admin</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
