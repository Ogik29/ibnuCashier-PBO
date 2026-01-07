<%@ page import="dao.*, model.*, java.util.*, java.sql.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User user = (User) session.getAttribute("user");
    if(user == null || !(user instanceof Admin)) { response.sendRedirect("index.jsp"); return; }
    
    ProductDAO pDao = new ProductDAO();
    CategoryDAO cDao = new CategoryDAO();
    StockOpnamDAO oDao = new StockOpnamDAO();
    PembelianDAO beliDao = new PembelianDAO();

    // Logic Tab & Filter
    String tab = request.getParameter("tab") == null ? "produk" : request.getParameter("tab");
    String search = request.getParameter("search");
    int catFilter = 0;
    try { catFilter = Integer.parseInt(request.getParameter("cat_filter")); } catch(Exception e){}
    List<Product> products = pDao.searchProducts(search, catFilter);
    List<Category> cats = cDao.getAll();
    
    // Logic Approve/Reject RESTOCK
    String action = request.getParameter("action");
    if("approve_faktur".equals(action)) {
        int fid = Integer.parseInt(request.getParameter("id"));
        if(beliDao.approveFaktur(fid, user.getUserID())) { response.sendRedirect("dashboard-admin.jsp?tab=restock&msg=success_approve"); return; }
    } else if("reject_faktur".equals(action)) {
        int fid = Integer.parseInt(request.getParameter("id"));
        if(beliDao.rejectFaktur(fid, user.getUserID())) { response.sendRedirect("dashboard-admin.jsp?tab=restock&msg=rejected"); return; }
    }
%>
<!DOCTYPE html>
<html>
<head><title>Admin Dashboard</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
<body>
<nav class="navbar navbar-dark bg-dark px-4">
    <span class="navbar-brand">Admin Panel</span>
    <div>
        <a href="sales-history.jsp" class="btn btn-info btn-sm me-2">History Checkout</a>
        <a href="auth?action=logout" class="btn btn-danger btn-sm">Logout</a>
    </div>
</nav>

<div class="container mt-4">
    
    <!-- NOTIFIKASI -->
    <% String msg = request.getParameter("msg"); 
       if("success_approve".equals(msg)){ %><div class="alert alert-success">Faktur Disetujui! Stok telah bertambah.</div><% } 
       if("rejected".equals(msg)){ %><div class="alert alert-warning">Faktur Ditolak / Dibatalkan.</div><% } %>

    <ul class="nav nav-tabs mb-3">
        <li class="nav-item"><a class="nav-link <%= tab.equals("produk")?"active":"" %>" href="?tab=produk">Data Produk</a></li>
        <!-- JUDUL TAB RESTOCK DIUBAH AGAR SESUAI FUNGSINYA -->
        <li class="nav-item"><a class="nav-link <%= tab.equals("restock")?"active":"" %>" href="?tab=restock">Approval Restock (Faktur)</a></li>
        <li class="nav-item"><a class="nav-link <%= tab.equals("opnam")?"active":"" %>" href="?tab=opnam">Approval Opnam (Fisik)</a></li>
    </ul>

    <!-- TAB 1: CRUD PRODUK (TIDAK BERUBAH) -->
    <% if(tab.equals("produk")) { %>
        <!-- (Kode bagian ini sama seperti sebelumnya, menampilkan list produk, search, edit, delete) -->
        <div class="row mb-3">
            <div class="col-md-4">
                <div class="card p-3 bg-light shadow-sm">
                    <h6>Master Data Produk</h6>
                    <form action="product" method="post" class="row g-2">
                        <input type="hidden" name="action" value="add">
                        <div class="col-4"><input name="sku" class="form-control form-control-sm" placeholder="SKU" required></div>
                        <div class="col-8"><input name="nama" class="form-control form-control-sm" placeholder="Nama" required></div>
                        <div class="col-6"><input name="harga" type="number" class="form-control form-control-sm" placeholder="Harga Jual" required></div>
                        <div class="col-6"><select name="kategori_id" class="form-select form-select-sm"><% for(Category c : cats){ %><option value="<%=c.getKategoriID()%>"><%=c.getNamaKategori()%></option><% } %></select></div>
                        <div class="col-12 mt-2"><button class="btn btn-success btn-sm w-100">Simpan Produk</button></div>
                    </form>
                </div>
            </div>
            <div class="col-md-8">
                 <form class="row g-2 mb-2 bg-light p-2" method="GET">
                    <input type="hidden" name="tab" value="produk">
                    <div class="col"><input name="search" class="form-control form-control-sm" placeholder="Cari..." value="<%=search!=null?search:""%>"></div>
                    <div class="col-auto"><button class="btn btn-primary btn-sm">Cari</button></div>
                 </form>
                 <div style="height:450px; overflow-y:auto; border:1px solid #ddd;">
                    <table class="table table-striped table-hover mb-0">
                        <thead class="table-light sticky-top"><tr><th>SKU</th><th>Nama</th><th>Harga</th><th>Stok</th><th>#</th></tr></thead>
                        <tbody><% for(Product p : products) { %>
                        <tr>
                            <td><%= p.getSKU() %></td>
                            <td><%= p.getNamaProduk() %></td>
                            <td><%= (int)p.getHargaJual() %></td>
                            <td class="<%=p.getStok()<=0?"text-danger":""%>"><%= p.getStok() %></td>
                            <td><a href="#" class="btn btn-sm btn-warning py-0" onclick="editProduct('<%=p.getSKU()%>','<%=p.getNamaProduk()%>',<%= (int)p.getHargaJual()%>)">Edit</a></td>
                        </tr><% } %></tbody>
                    </table>
                 </div>
            </div>
        </div>
        
    <!-- TAB 2: APPROVAL RESTOCK (MENGGANTIKAN INPUT MANUAL ADMIN) -->
    <% } else if(tab.equals("restock")) { 
        ResultSet rsDraft = beliDao.getDraftInvoices(); // Mengambil data pending
    %>
        <h4 class="mb-3">Daftar Pending Invoice (Input Kasir)</h4>
        <div class="alert alert-info py-2">Faktur dibawah ini dibuat oleh Kasir. Cek kesesuaian fisik, lalu <b>Approve</b> untuk menambah stok ke sistem.</div>
        
        <table class="table table-bordered shadow-sm">
            <thead class="table-dark">
                <tr>
                    <th>No Faktur</th>
                    <th>Tgl Input</th>
                    <th class="text-center">Jum Item</th>
                    <th class="text-end">Total Nilai (Rp)</th>
                    <th class="text-center">Action</th>
                </tr>
            </thead>
            <tbody>
                <% boolean hasData = false;
                   while(rsDraft.next()) { hasData=true; %>
                <tr>
                    <td class="fw-bold"><%= rsDraft.getString("no_faktur") %></td>
                    <td><%= rsDraft.getString("waktu") %></td>
                    <td class="text-center"><%= rsDraft.getInt("jum_item") %></td>
                    <td class="text-end fw-bold"><%= String.format("%,.0f", rsDraft.getDouble("total_nilai")) %></td>
                    <td class="text-center">
                        <div class="btn-group">
                            <button type="button" class="btn btn-sm btn-outline-primary" onclick="alert('Fitur lihat detail item belum dipasang di kode snippet ini.\nNamun Logika Approve akan memproses semua item didalam faktur ID: <%= rsDraft.getInt("faktur_id") %>')">Lihat Item</button>
                            
                            <!-- TOMBOL APPROVE -->
                            <form method="POST" onsubmit="return confirm('Stok akan bertambah permanen. Lanjutkan?')">
                                <input type="hidden" name="action" value="approve_faktur">
                                <input type="hidden" name="id" value="<%= rsDraft.getInt("faktur_id") %>">
                                <button class="btn btn-sm btn-success">✓ APPROVE</button>
                            </form>

                            <!-- TOMBOL REJECT -->
                            <form method="POST" onsubmit="return confirm('Hapus faktur ini?')">
                                <input type="hidden" name="action" value="reject_faktur">
                                <input type="hidden" name="id" value="<%= rsDraft.getInt("faktur_id") %>">
                                <button class="btn btn-sm btn-danger">✕</button>
                            </form>
                        </div>
                    </td>
                </tr>
                <% } 
                   if(!hasData) { %> 
                   <tr><td colspan="5" class="text-center py-4 text-muted">Belum ada faktur restock dari kasir.</td></tr> 
                <% } %>
            </tbody>
        </table>

    <!-- TAB 3: APPROVAL OPNAM (TETAP SAMA) -->
    <% } else if(tab.equals("opnam")) { ResultSet rsOpnam = oDao.getPendingOpnames(); %>
        <h4>Approval Opnam Fisik</h4>
        <table class="table table-bordered">
            <thead class="table-light"><tr><th>ID</th><th>User</th><th>Waktu</th><th>Action</th></tr></thead>
            <tbody>
            <% while(rsOpnam.next()) { %>
            <tr>
                <td>#<%= rsOpnam.getInt("opnam_id") %></td>
                <td><%= rsOpnam.getString("creator") %></td>
                <td><%= rsOpnam.getString("waktu") %></td>
                <td>
                    <form action="opnam" method="post" style="display:inline"><input type="hidden" name="action" value="approve"><input type="hidden" name="id" value="<%=rsOpnam.getInt("opnam_id")%>"><button class="btn btn-success btn-sm">✓</button></form>
                    <form action="opnam" method="post" style="display:inline"><input type="hidden" name="action" value="reject"><input type="hidden" name="id" value="<%=rsOpnam.getInt("opnam_id")%>"><button class="btn btn-danger btn-sm">✕</button></form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    <% } %>
</div>

<!-- Modal Edit Produk Tetap Sama -->
<div class="modal fade" id="editModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="product" method="post" class="modal-content">
            <div class="modal-header"><h5>Edit Produk</h5><button class="btn-close" data-bs-dismiss="modal"></button></div>
            <div class="modal-body">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="sku" id="editSku">
                <input name="nama" id="editNama" class="form-control mb-2" required>
                <input name="harga" id="editHarga" class="form-control mb-2" required>
            </div>
            <div class="modal-footer"><button class="btn btn-warning">Update</button></div>
        </form>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
function editProduct(sku, nama, harga){
    document.getElementById('editSku').value=sku;
    document.getElementById('editNama').value=nama;
    document.getElementById('editHarga').value=harga;
    new bootstrap.Modal(document.getElementById('editModal')).show();
}
</script>
</body>
</html>