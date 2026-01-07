<%@ page import="model.*, dao.*, java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User user = (User) session.getAttribute("user");
    if(user == null || !(user instanceof Kasir)) { response.sendRedirect("index.jsp"); return; }

    List<SaleItem> cart = (List<SaleItem>) session.getAttribute("cart");
    ProductDAO productDAO = new ProductDAO();

    String keyword = request.getParameter("search"); 
    List<Product> productList = productDAO.searchProducts(keyword, 0); 
%>
<!DOCTYPE html>
<html>
<head>
    <title>KASIR POS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style> .input-qty { width: 60px; text-align: center; } </style>
</head>
<body class="bg-light">
<nav class="navbar navbar-dark bg-primary px-4 shadow-sm">
    <span class="navbar-brand">KASIR: <%= user.getUsername() %></span>
    <div>
        <a href="sales-history.jsp" class="btn btn-info btn-sm text-white me-2">History Transaksi</a>
        
        <!-- BUTTON MENU INPUT STOK (RESTOCK & OPNAM) -->
        <div class="btn-group me-2">
            <button class="btn btn-warning btn-sm dropdown-toggle" data-bs-toggle="dropdown">Input Stok</button>
            <ul class="dropdown-menu">
                <li><a class="dropdown-item" href="#" data-bs-toggle="modal" data-bs-target="#restockModal">Input Pembelian (Faktur)</a></li>
                <li><a class="dropdown-item" href="#" data-bs-toggle="modal" data-bs-target="#opnamModal">Input Opnam (Fisik)</a></li>
            </ul>
        </div>
        
        <a href="auth?action=logout" class="btn btn-danger btn-sm">Logout</a>
    </div>
</nav>

<div class="container-fluid mt-4">
    
    <!-- Notifikasi Feedback -->
    <% String msg = request.getParameter("msg"); 
       if("restock_ok".equals(msg)) { %><div class="alert alert-success">Item berhasil ditambahkan ke Faktur Draf! Tunggu Admin Approve.</div><% } 
       if("restock_fail".equals(msg)) { %><div class="alert alert-danger">Gagal Input Restock! Cek SKU atau Database.</div><% } %>

    <div class="row">
        <!-- (BAGIAN KATALOG PRODUK TETAP SAMA SEPERTI YANG LAMA) -->
        <div class="col-md-7">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 text-primary">Katalog</h5>
                    <form method="get" class="d-flex">
                        <input type="text" name="search" class="form-control form-control-sm me-2" placeholder="Cari Nama/SKU..." value="<%= keyword != null ? keyword : "" %>">
                        <button type="submit" class="btn btn-sm btn-outline-primary">Cari</button>
                    </form>
                </div>
                <div class="card-body p-0" style="height:500px; overflow-y:auto;">
                    <table class="table table-hover mb-0 align-middle">
                        <thead class="table-light sticky-top"><tr><th>SKU</th><th>Nama</th><th>Harga</th><th>Stok</th><th>Aksi</th></tr></thead>
                        <tbody>
                        <% for(Product p : productList) { %>
                            <tr>
                                <td><small class="badge bg-secondary"><%= p.getSKU() %></small></td>
                                <td><%= p.getNamaProduk() %> <br> <small class="text-muted"><%= p.getKategori() %></small></td>
                                <td><%= (int)p.getHargaJual() %></td>
                                <td class="<%= p.getStok()<=0 ? "text-danger":"" %> fw-bold"><%= p.getStok() %></td>
                                <td>
                                    <!-- POS BUTTON -->
                                    <form action="PosServlet" method="post" class="d-flex">
                                        <input type="hidden" name="action" value="add">
                                        <input type="hidden" name="sku" value="<%= p.getSKU() %>">
                                        <% if(p.getStok() > 0) { %>
                                        <!--<input type="number" name="qty" value="1" min="1" max="<%= p.getStok() %>" class="form-control form-control-sm input-qty me-1">-->
                                        <button class="btn btn-sm btn-primary">+</button>
                                        <% } else { %><button class="btn btn-sm btn-secondary" disabled>Habis</button><% } %>
                                    </form>
                                </td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- (BAGIAN KERANJANG PENJUALAN TETAP SAMA) -->
        <div class="col-md-5">
            <div class="card shadow-sm">
                <div class="card-header bg-white d-flex justify-content-between">
                    <h5>Keranjang</h5>
                    <form action="PosServlet" method="post"><input type="hidden" name="action" value="reset"><button class="btn btn-sm btn-outline-danger">Reset</button></form>
                </div>
                <div class="card-body">
                    <table class="table table-sm">
                        <% double total = 0; if(cart!=null){ for(SaleItem i : cart){ total+=i.getSubtotal(); %>
                        <tr><td><%= i.getProduct().getNamaProduk() %></td><td>x<%= i.getQty() %></td><td class="text-end"><%= (int)i.getSubtotal() %></td></tr>
                        <% }} %>
                    </table>
                    <div class="border-top mt-3 pt-2 text-end">
                        <h3>Rp <%= (int)total %></h3>
                        <form action="PosServlet" method="post"><input type="hidden" name="action" value="checkout"><button class="btn btn-success w-100 btn-lg" <%= (cart==null||cart.isEmpty())?"disabled":"" %>>BAYAR</button></form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- ========================= MODAL 1: RESTOCK SUPPLIER (FAKTUR PEMBELIAN) ========================= -->
<div class="modal fade" id="restockModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header bg-warning text-dark"><h5 class="modal-title">Input Pembelian Supplier (Restock)</h5><button class="btn-close" data-bs-dismiss="modal"></button></div>
            <form action="PosServlet" method="post">
                <input type="hidden" name="action" value="input_restock_kasir"> <!-- Action khusus -->
                <div class="modal-body">
                    <p class="small text-muted">Masukkan nomor faktur yang SAMA untuk item dalam satu invoice.</p>
                    <div class="mb-3">
                        <label>Nomor Faktur / Invoice Supplier</label>
                        <!-- Tips: Value faktur bisa dipersistent via session agar kasir tdk ngetik ulang terus menerus jika banyak item -->
                        <input type="text" name="noFaktur" class="form-control" placeholder="INV-2026-XXXX" required> 
                    </div>
                    <div class="row g-2 mb-2">
                         <div class="col-6">
                             <label>SKU Produk</label>
                             <input type="text" name="sku" class="form-control" placeholder="Scan Barcode" required autofocus>
                         </div>
                         <div class="col-6">
                             <label>Jumlah Masuk (Qty)</label>
                             <input type="number" name="qty" class="form-control" min="1" required>
                         </div>
                    </div>
                    <div class="mb-2">
                        <label>Harga Beli Satuan (Dari Supplier)</label>
                        <input type="number" name="hargaBeli" class="form-control" required>
                    </div>
                </div>
                <div class="modal-footer"><button class="btn btn-warning w-100">Simpan Item ke Draf</button></div>
            </form>
        </div>
    </div>
</div>

<!-- ========================= MODAL 2: STOK OPNAM ========================= -->
<div class="modal fade" id="opnamModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header"><h5 class="modal-title">Stok Opnam (Koreksi Fisik)</h5><button class="btn-close" data-bs-dismiss="modal"></button></div>
            <form action="opnam" method="post">
                <div class="modal-body">
                    <input type="hidden" name="action" value="create_draft">
                    <div class="mb-2"><input type="text" name="sku" class="form-control" placeholder="SKU" required></div>
                    <div class="mb-2"><input type="number" name="qty" class="form-control" placeholder="Selisih (+/-)" required></div>
                </div>
                <div class="modal-footer"><button class="btn btn-primary">Kirim Laporan</button></div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>