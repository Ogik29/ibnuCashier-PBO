<%-- 
    Document   : pos
    Created on : 14 Dec 2025, 15.20.26
    Author     : dimas
--%>

<%@ page import="model.*, dao.*, java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User user = (User) session.getAttribute("user");
    if(user == null || !(user instanceof Kasir)) { response.sendRedirect("index.jsp"); return; }

    List<SaleItem> cart = (List<SaleItem>) session.getAttribute("cart");
    ProductDAO productDAO = new ProductDAO();

    // FITUR PENCARIAN
    String keyword = request.getParameter("search"); // Ambil keyword dari URL
    List<Product> productList = productDAO.searchProducts(keyword, 0); // Cari data
%>
<!DOCTYPE html>
<html>
<head>
    <title>KASIR POS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-dark bg-primary px-4 shadow-sm">
    <span class="navbar-brand">KASIR: <%= user.getUsername() %></span>
    <div>
        <a href="sales-history.jsp" class="btn btn-info btn-sm text-white me-2">Riwayat Transaksi</a>
        <button class="btn btn-light btn-sm me-2" data-bs-toggle="modal" data-bs-target="#opnamModal">Opnam</button>
        <a href="auth?action=logout" class="btn btn-danger btn-sm">Logout</a>
    </div>
</nav>

<div class="container-fluid mt-4">
    <div class="row">
        <!-- List Produk -->
        <div class="col-md-7">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 text-primary">Katalog</h5>
                    
                    <!-- FORM SEARCH -->
                    <form method="get" class="d-flex">
                        <input type="text" name="search" class="form-control form-control-sm me-2" placeholder="Cari Nama/SKU..." value="<%= keyword != null ? keyword : "" %>">
                        <button type="submit" class="btn btn-sm btn-outline-primary">Cari</button>
                    </form>
                </div>
                <div class="card-body p-0" style="height:500px; overflow-y:auto;">
                    <table class="table table-hover mb-0">
                        <thead class="table-light sticky-top">
                            <tr><th>SKU</th><th>Nama</th><th>Harga</th><th>Kategori</th><th>Stok</th><th>Aksi</th></tr>
                        </thead>
                        <tbody>
                        <% for(Product p : productList) { %>
                            <tr>
                                <td><small class="badge bg-secondary"><%= p.getSKU() %></small></td>
                                <td><%= p.getNamaProduk() %></td>
                                <td><%= (int)p.getHargaJual() %></td>
                                <td><%= p.getKategori() %></td>
                                <td class="<%= p.getStok()<=0 ? "text-danger":"" %>"><%= p.getStok() %></td>
                                <td>
                                    <form action="PosServlet" method="post">
                                        <input type="hidden" name="action" value="add">
                                        <input type="hidden" name="sku" value="<%= p.getSKU() %>">
                                        <button class="btn btn-sm btn-primary" <%= p.getStok()<=0?"disabled":"" %>>+</button>
                                    </form>
                                </td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Keranjang -->
        <div class="col-md-5">
            <div class="card shadow-sm">
                <div class="card-header bg-white d-flex justify-content-between">
                    <h5>Keranjang</h5>
                    <form action="PosServlet" method="post"><input type="hidden" name="action" value="reset"><button class="btn btn-sm btn-outline-danger">Reset</button></form>
                </div>
                <div class="card-body">
                    <table class="table table-sm">
                        <% 
                        double total = 0;
                        if(cart!=null){ for(SaleItem i : cart){ total+=i.getSubtotal(); %>
                        <tr>
                            <td><%= i.getProduct().getNamaProduk() %></td>
                            <td>x<%= i.getQty() %></td>
                            <td class="text-end"><%= (int)i.getSubtotal() %></td>
                        </tr>
                        <% }} %>
                    </table>
                    <div class="border-top mt-3 pt-2 text-end">
                        <h3>Rp <%= (int)total %></h3>
                        <form action="PosServlet" method="post">
                            <input type="hidden" name="action" value="checkout">
                            <button class="btn btn-success w-100 btn-lg" <%= (cart==null||cart.isEmpty())?"disabled":"" %>>BAYAR</button>
                        </form>
                    </div>
                    <% if("success".equals(request.getParameter("msg"))) { %><div class="alert alert-success mt-2">Sukses!</div><% } %>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Opnam -->
<div class="modal fade" id="opnamModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header"><h5 class="modal-title">Stok Opnam (ADD STOCK)</h5><button class="btn-close" data-bs-dismiss="modal"></button></div>
            <form action="opnam" method="post">
                <div class="modal-body">
                    <input type="hidden" name="action" value="create_draft">
                    <p class="text-muted">Masukkan jumlah barang yang INGIN DITAMBAHKAN ke sistem.</p>
                    <div class="mb-2"><input type="text" name="sku" class="form-control" placeholder="SKU" required></div>
                    <div class="mb-2"><input type="number" name="qty" class="form-control" placeholder="Jumlah Tambahan" required></div>
                </div>
                <div class="modal-footer"><button class="btn btn-primary">Kirim</button></div>
            </form>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>