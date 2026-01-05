<%-- 
    Document   : dashboard-admin
    Created on : 5 Jan 2026, 15.10.28
    Author     : dimas
--%>

<%@ page import="dao.*, model.*, java.util.*, java.sql.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User user = (User) session.getAttribute("user");
    if(user == null || !(user instanceof Admin)) { response.sendRedirect("index.jsp"); return; }
    
    ProductDAO pDao = new ProductDAO();
    CategoryDAO cDao = new CategoryDAO();
    StockOpnamDAO oDao = new StockOpnamDAO();

    List<Category> cats = cDao.getAll();
    ResultSet rsOpnam = oDao.getPendingOpnames();

    // === LOGIK FILTER BARU ===
    // 1. Ambil Parameter
    String search = request.getParameter("search");
    String catFilterParam = request.getParameter("cat_filter");
    int catFilter = 0;
    
    // 2. Parsing (dengan safety check)
    if(catFilterParam != null && !catFilterParam.isEmpty()) {
        try { catFilter = Integer.parseInt(catFilterParam); } catch(Exception e){}
    }
    
    // 3. Panggil DAO Search
    List<Product> products = pDao.searchProducts(search, catFilter);

    String tab = request.getParameter("tab") == null ? "produk" : request.getParameter("tab");
%>
<!DOCTYPE html>
<html>
<head><title>Admin Dashboard</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
<body>
<nav class="navbar navbar-dark bg-dark px-4">
    <span class="navbar-brand">Admin: <%= user.getUsername() %></span>
    <div>
        <a href="sales-history.jsp" class="btn btn-info btn-sm me-2">History Checkout</a>
        <a href="auth?action=logout" class="btn btn-danger btn-sm">Logout</a>
    </div>
</nav>

<div class="container mt-4">
    <!-- Feedback Message -->
    <% String msg = request.getParameter("msg"); 
       if("approved".equals(msg)) { %><div class="alert alert-success">Opnam disetujui. Stok telah bertambah.</div><% } 
       else if("rejected".equals(msg)) { %><div class="alert alert-danger">Opnam DITOLAK. Stok tidak berubah.</div><% } %>

    <ul class="nav nav-tabs mb-3">
        <li class="nav-item"><a class="nav-link <%= tab.equals("produk")?"active":"" %>" href="?tab=produk">Produk</a></li>
        <li class="nav-item"><a class="nav-link <%= tab.equals("opnam")?"active":"" %>" href="?tab=opnam">Opnam Pending</a></li>
    </ul>

    <!-- ======================= TAB PRODUK ======================= -->
    <% if(tab.equals("produk")) { %>
        <div class="row mb-3">
            <div class="col-md-5">
                <div class="card p-3 bg-light shadow-sm">
                    <h6>Tambah Produk Baru</h6>
                    <form action="product" method="post" class="row g-2">
                        <input type="hidden" name="action" value="add">
                        <div class="col-4"><input name="sku" class="form-control form-control-sm" placeholder="SKU" required></div>
                        <div class="col-8"><input name="nama" class="form-control form-control-sm" placeholder="Nama Produk" required></div>
                        <div class="col-6"><input name="harga" type="number" class="form-control form-control-sm" placeholder="Harga Jual" required></div>
                        <div class="col-6">
                            <select name="kategori_id" class="form-select form-select-sm">
                                <% for(Category c : cats) { %>
                                    <option value="<%=c.getKategoriID()%>"><%=c.getNamaKategori()%></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="col-12 mt-2">
                            <button class="btn btn-success btn-sm w-100">Simpan Produk</button>
                        </div>
                    </form>
                </div>
            </div>
            
            <div class="col-md-7">
                 <!-- FITUR SEARCH & FILTER -->
                 <form class="row g-2 mb-2 align-items-center bg-light p-2 rounded" method="GET">
                    <input type="hidden" name="tab" value="produk">
                    <div class="col-auto">
                        <!-- Dropdown Filter Kategori -->
                        <select name="cat_filter" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="0">-- Semua Kategori --</option>
                            <% for(Category c : cats) { %>
                                <option value="<%=c.getKategoriID()%>" <%= catFilter==c.getKategoriID() ? "selected" : "" %>>
                                    <%= c.getNamaKategori() %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col">
                        <input name="search" class="form-control form-control-sm" placeholder="Cari Nama/SKU..." value="<%=search!=null?search:""%>">
                    </div>
                    <div class="col-auto">
                        <button class="btn btn-primary btn-sm">Filter</button>
                        <a href="?tab=produk" class="btn btn-outline-secondary btn-sm">Reset</a>
                    </div>
                 </form>
                 
                 <div style="height:400px; overflow-y:auto; border:1px solid #ddd;">
                 <table class="table table-striped table-hover mb-0">
                    <thead class="table-light sticky-top">
                        <tr><th>SKU</th><th>Nama</th><th>Kategori</th><th>Harga</th><th>Stok</th><th>#</th></tr>
                    </thead>
                    <tbody>
                        <% for(Product p : products) { %>
                        <tr>
                            <td><%= p.getSKU() %></td>
                            <td><%= p.getNamaProduk() %></td>
                            <td><span class="badge bg-secondary"><%= p.getKategori() %></span></td>
                            <td><%= (int)p.getHargaJual() %></td>
                            <td class="<%=p.getStok()<=0?"text-danger fw-bold":""%>"><%= p.getStok() %></td>
                            <td>
                                <a href="product?action=delete&sku=<%=p.getSKU()%>" class="text-danger fw-bold text-decoration-none" onclick="return confirm('Hapus?')">X</a>
                                <a href="#" class="text-warning fw-bold text-decoration-none ms-2" 
                                    onclick="editProduct('<%=p.getSKU()%>', '<%=p.getNamaProduk()%>', '<%= (int)p.getHargaJual() %>', '<%=p.getKategoriID()%>')">
                                    Edit
                                </a>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                 </table>
                 </div>
            </div>
        </div>
        
    <!-- ======================= TAB OPNAM ======================= -->
    <% } else if(tab.equals("opnam")) { %>
        <h4>Persetujuan Stok Opnam</h4>
        <div class="alert alert-info py-2">Setujui untuk menambah stok, Tolak jika laporan salah.</div>
        <table class="table table-bordered">
            <thead class="table-light"><tr><th>ID Opnam</th><th>Kasir</th><th>Waktu</th><th class="text-center" style="width:200px">Action</th></tr></thead>
            <tbody>
            <% while(rsOpnam.next()) { %>
            <tr>
                <td>#<%= rsOpnam.getInt("opnam_id") %></td>
                <td><%= rsOpnam.getString("creator") %></td>
                <td><%= rsOpnam.getString("waktu") %></td>
                <td class="text-center">
                    <!-- Form Approve (Tombol Hijau) -->
                    <form action="opnam" method="post" style="display:inline-block">
                        <input type="hidden" name="action" value="approve">
                        <input type="hidden" name="id" value="<%= rsOpnam.getInt("opnam_id") %>">
                        <button class="btn btn-success btn-sm">✓ Setuju</button>
                    </form>

                    <!-- Form Reject (Tombol Merah - FITUR BARU) -->
                    <form action="opnam" method="post" style="display:inline-block">
                        <input type="hidden" name="action" value="reject">
                        <input type="hidden" name="id" value="<%= rsOpnam.getInt("opnam_id") %>">
                        <button class="btn btn-danger btn-sm" onclick="return confirm('Tolak laporan stok ini?')">✕ Tolak</button>
                    </form>
                </td>
            </tr>
            <% } %>
            <% if(!rsOpnam.isBeforeFirst()) { %>
                <tr><td colspan="4" class="text-center text-muted">Tidak ada opnam pending.</td></tr>
            <% } %>
            </tbody>
        </table>
    <% } %>
</div>

<!-- Modal EDIT Produk (Copy Paste yg lama, logic JS sama) -->
<div class="modal fade" id="editModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="product" method="post" class="modal-content">
            <div class="modal-header"><h5>Edit Produk</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
            <div class="modal-body">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="sku" id="editSku">
                
                <div class="mb-2"><label>Nama</label><input name="nama" id="editNama" class="form-control" required></div>
                <div class="mb-2"><label>Harga</label><input name="harga" type="number" id="editHarga" class="form-control" required></div>
                <div class="mb-2"><label>Kategori</label>
                    <select name="kategori_id" id="editKat" class="form-select">
                        <% for(Category c : cats) { %><option value="<%=c.getKategoriID()%>"><%=c.getNamaKategori()%></option><% } %>
                    </select>
                </div>
            </div>
            <div class="modal-footer"><button class="btn btn-warning">Update</button></div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function editProduct(sku, nama, harga, katId) {
        document.getElementById('editSku').value = sku;
        document.getElementById('editNama').value = nama;
        document.getElementById('editHarga').value = harga;
        document.getElementById('editKat').value = katId;
        var myModal = new bootstrap.Modal(document.getElementById('editModal'));
        myModal.show();
    }
</script>
</body>
</html>