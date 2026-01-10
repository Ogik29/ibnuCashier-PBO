<%@ page import="dao.*, model.*, java.util.*, java.sql.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // 1. CEK SESSION ADMIN
    User user = (User) session.getAttribute("user");
    if(user == null || !(user instanceof Admin)) { response.sendRedirect("index.jsp"); return; }
    
    // 2. INISIALISASI DAO
    ProductDAO pDao = new ProductDAO();
    CategoryDAO cDao = new CategoryDAO();
    StockOpnamDAO oDao = new StockOpnamDAO();
    PembelianDAO beliDao = new PembelianDAO();

    // 3. LOGIK FILTER TAB & PENCARIAN
    String tab = request.getParameter("tab") == null ? "produk" : request.getParameter("tab");
    String search = request.getParameter("search");
    int catFilter = 0;
    try { 
        if(request.getParameter("cat_filter") != null) 
            catFilter = Integer.parseInt(request.getParameter("cat_filter")); 
    } catch(Exception e){}

    // Ambil Data Produk & Kategori
    List<Product> products = pDao.searchProducts(search, catFilter);
    List<Category> cats = cDao.getAll();
    
    // 4. LOGIK HANDLING ACTION DI JSP (Khusus Approve/Reject RESTOCK)
    // Fitur lain seperti Product dan Category ditangani oleh Servlet masing-masing
    String action = request.getParameter("action");
    if("approve_faktur".equals(action)) {
        int fid = Integer.parseInt(request.getParameter("id"));
        if(beliDao.approveFaktur(fid, user.getUserID())) { 
            response.sendRedirect("dashboard-admin.jsp?tab=restock&msg=success_approve"); 
            return; 
        }
    } else if("reject_faktur".equals(action)) {
        int fid = Integer.parseInt(request.getParameter("id"));
        if(beliDao.rejectFaktur(fid, user.getUserID())) { 
            response.sendRedirect("dashboard-admin.jsp?tab=restock&msg=rejected"); 
            return; 
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .nav-tabs .nav-link.active { font-weight: bold; border-top: 3px solid #0d6efd; color: #0d6efd; }
        .nav-tabs .nav-link { color: #555; }
    </style>
</head>
<body class="bg-light">

<!-- NAVBAR -->
<nav class="navbar navbar-dark bg-dark px-4 shadow-sm sticky-top">
    <span class="navbar-brand mb-0 h1">Admin Panel: <%= user.getUsername() %></span>
    <div>
        <a href="sales-history.jsp" class="btn btn-info btn-sm me-2 text-white">History Checkout</a>
        <a href="auth?action=logout" class="btn btn-danger btn-sm">Logout</a>
    </div>
</nav>

<div class="container mt-4 pb-5">
    
    <!-- SYSTEM NOTIFICATIONS -->
    <% String msg = request.getParameter("msg"); 
       if("success_approve".equals(msg)){ %><div class="alert alert-success alert-dismissible fade show">Faktur Disetujui! Stok produk telah bertambah.<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div><% } 
       else if("rejected".equals(msg)){ %><div class="alert alert-warning alert-dismissible fade show">Faktur Ditolak / Dibatalkan.<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div><% }
       else if("updated".equals(msg)){ %><div class="alert alert-success alert-dismissible fade show">Data Produk berhasil diperbarui!<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div><% }
       else if("deleted".equals(msg)){ %><div class="alert alert-success alert-dismissible fade show">Produk berhasil dihapus dari sistem.<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div><% }
       else if("fail_delete".equals(msg)){ %><div class="alert alert-danger alert-dismissible fade show">Gagal hapus! Produk mungkin sedang digunakan di Transaksi/Faktur.<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div><% }
       else if("err_sku_duplicate".equals(msg)){ %><div class="alert alert-danger alert-dismissible fade show">Gagal tambah! SKU (Barcode) sudah terdaftar.<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div><% }
       else if("saved".equals(msg)){ %><div class="alert alert-success alert-dismissible fade show">Produk baru berhasil disimpan.<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div><% }
    %>

    <!-- NAVIGATION TABS -->
    <ul class="nav nav-tabs mb-3 shadow-sm bg-white rounded-top px-2 pt-2">
        <li class="nav-item">
            <a class="nav-link <%= tab.equals("produk")?"active":"" %>" href="?tab=produk">Master Produk</a>
        </li>
        <!-- TAB BARU: KATEGORI -->
        <li class="nav-item">
            <a class="nav-link <%= tab.equals("kategori")?"active":"" %>" href="?tab=kategori">Master Kategori</a>
        </li>
        <li class="nav-item">
            <a class="nav-link <%= tab.equals("restock")?"active":"" %>" href="?tab=restock">Approval Restock (Faktur)</a>
        </li>
        <li class="nav-item">
            <a class="nav-link <%= tab.equals("opnam")?"active":"" %>" href="?tab=opnam">Approval Opnam (Fisik)</a>
        </li>
    </ul>

    <!-- ==================== TAB 1: MASTER DATA PRODUK (CRUD) ==================== -->
    <% if(tab.equals("produk")) { %>
        <div class="row mb-3">
            <!-- Form Tambah Produk -->
            <div class="col-md-4">
                <div class="card p-3 shadow-sm border-0 sticky-top" style="top: 80px; z-index:0;">
                    <h6 class="border-bottom pb-2 mb-3 fw-bold text-primary">Tambah Produk Baru</h6>
                    <form action="product" method="post" class="row g-2">
                        <input type="hidden" name="action" value="add">
                        
                        <div class="col-4">
                            <label class="form-label small">SKU</label>
                            <input name="sku" class="form-control form-control-sm" placeholder="Contoh: P001" required>
                        </div>
                        <div class="col-8">
                            <label class="form-label small">Nama Produk</label>
                            <input name="nama" class="form-control form-control-sm" placeholder="Nama Barang" required>
                        </div>
                        
                        <div class="col-6">
                            <label class="form-label small">Harga Jual</label>
                            <input name="harga" type="number" class="form-control form-control-sm" required>
                        </div>
                        <!-- ADMIN BISA NAMBAH STOCK AWAL LANGSUNG -->
                        <div class="col-6">
                            <label class="form-label small">Stok Awal</label>
                            <input name="stok" type="number" class="form-control form-control-sm" value="0" required>
                        </div>
                        
                        <div class="col-12">
                            <label class="form-label small">Kategori</label>
                            <select name="kategori_id" class="form-select form-select-sm">
                                <% for(Category c : cats){ %><option value="<%=c.getKategoriID()%>"><%=c.getNamaKategori()%></option><% } %>
                            </select>
                        </div>
                        <div class="col-12 mt-3">
                            <button class="btn btn-primary btn-sm w-100 fw-bold">Simpan Produk</button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Tabel List Produk -->
            <div class="col-md-8">
                <!-- Search & Filter -->
                 <form class="row g-2 mb-3 bg-white p-2 rounded shadow-sm mx-1 align-items-end" method="GET">
                    <input type="hidden" name="tab" value="produk">
                    <div class="col-auto">
                        <label class="small text-muted">Kategori</label>
                        <select name="cat_filter" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="0">Semua</option>
                            <% for(Category c : cats){ %>
                                <option value="<%=c.getKategoriID()%>" <%=catFilter==c.getKategoriID()?"selected":""%>><%=c.getNamaKategori()%></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col">
                        <label class="small text-muted">Cari</label>
                        <input name="search" class="form-control form-control-sm" placeholder="Cari nama atau SKU..." value="<%=search!=null?search:""%>">
                    </div>
                    <div class="col-auto">
                        <button class="btn btn-outline-primary btn-sm px-4">Filter</button>
                    </div>
                 </form>

                 <!-- Tabel Data -->
                 <div class="bg-white rounded shadow-sm mx-1" style="height:450px; overflow-y:auto; border:1px solid #eee;">
                    <table class="table table-striped table-hover mb-0">
                        <thead class="table-light sticky-top">
                            <tr>
                                <th>SKU</th>
                                <th>Nama</th>
                                <th>Harga</th>
                                <th class="text-center">Stok</th>
                                <th class="text-center">Aksi</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% for(Product p : products) { %>
                        <tr>
                            <td><span class="badge bg-light text-dark border"><%= p.getSKU() %></span></td>
                            <td>
                                <%= p.getNamaProduk() %>
                                <br><small class="text-muted" style="font-size:0.75rem"><%= p.getKategori() %></small>
                            </td>
                            <td><%= String.format("%,.0f", p.getHargaJual()) %></td>
                            <td class="text-center <%= p.getStok() <= 0 ? "text-danger fw-bold" : "" %>">
                                <%= p.getStok() %>
                            </td>
                            <td class="text-center">
                                <div class="btn-group">
                                    <button class="btn btn-sm btn-outline-warning border-0" 
                                            onclick="editProduct('<%=p.getSKU()%>','<%=p.getNamaProduk()%>','<%= (int)p.getHargaJual() %>', '<%= p.getStok() %>', '<%=p.getKategoriID()%>')">
                                        Edit
                                    </button>
                                    <a href="product?action=delete&sku=<%=p.getSKU()%>" class="btn btn-sm btn-outline-danger border-0" onclick="return confirm('Hapus permanen produk: <%=p.getNamaProduk()%>?')">Hapus</a>
                                </div>
                            </td>
                        </tr>
                        <% } if(products.isEmpty()) { %>
                            <tr><td colspan="5" class="text-center py-5 text-muted">Data tidak ditemukan.</td></tr>
                        <% } %>
                        </tbody>
                    </table>
                 </div>
            </div>
        </div>

    <!-- ==================== TAB BARU: MASTER KATEGORI ==================== -->
    <% } else if(tab.equals("kategori")) { %>
        <div class="row">
            <!-- Kolom Input Kategori -->
            <div class="col-md-4">
                <div class="card p-4 shadow-sm border-0 bg-white">
                    <h6 class="mb-3 border-bottom pb-2 fw-bold text-primary">Kelola Kategori</h6>
                    <!-- Action ke CategoryServlet -->
                    <form action="category" method="post">
                        <input type="hidden" name="action" value="add">
                        <div class="mb-3">
                            <label class="form-label small text-muted">Nama Kategori Baru</label>
                            <input name="nama" class="form-control" placeholder="Contoh: Minuman, Sembako" required autofocus>
                        </div>
                        <button class="btn btn-primary w-100">Simpan Kategori</button>
                    </form>
<!--                    <div class="alert alert-info mt-3 small py-2">
                        <i class="bi bi-info-circle"></i> Kategori digunakan untuk mengelompokkan produk di halaman kasir dan laporan.
                    </div>-->
                </div>
            </div>

            <!-- Kolom List Kategori -->
            <div class="col-md-8">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-white py-3">
                        <h6 class="m-0 fw-bold text-secondary">Daftar Kategori Tersedia</h6>
                    </div>
                    <div class="card-body p-0">
                        <table class="table table-hover mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th width="10%" class="text-center">ID</th>
                                    <th>Nama Kategori</th>
                                    <th width="15%" class="text-center">Aksi</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% boolean adaKat = false;
                                   for(Category c : cats) { 
                                       adaKat = true; 
                                %>
                                <tr>
                                    <td class="text-center align-middle"><span class="badge bg-secondary"><%= c.getKategoriID() %></span></td>
                                    <td class="align-middle fw-bold text-dark"><%= c.getNamaKategori() %></td>
                                    <td class="text-center">
                                        <!-- Form Delete karena Servlet method POST -->
                                        <form action="category" method="post" onsubmit="return confirm('Hapus Kategori <%= c.getNamaKategori() %>?\nPERINGATAN: Pastikan tidak ada produk yang menggunakan kategori ini!')">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="<%= c.getKategoriID() %>">
                                            <button class="btn btn-sm btn-outline-danger border-0">Hapus</button>
                                        </form>
                                    </td>
                                </tr>
                                <% } 
                                   if(!adaKat) { %>
                                   <tr><td colspan="3" class="text-center py-4 text-muted">Belum ada kategori data.</td></tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

    <!-- ==================== TAB 2: APPROVAL RESTOCK (PEMBELIAN) ==================== -->
    <% } else if(tab.equals("restock")) { 
        // Mengambil list faktur yang statusnya DRAFT dari DAO
        ResultSet rsDraft = beliDao.getDraftInvoices(); 
    %>
        <h5 class="mb-3 text-primary">Daftar Pending Restock</h5>
        
        
        <div class="card shadow-sm border-0">
            <table class="table table-hover mb-0">
                <thead class="table-dark">
                    <tr>
                        <th>No Faktur</th>
                        <th>Waktu Input</th>
                        <th class="text-center">Total Qty (Fisik)</th>
                        <th class="text-end">Total Nilai (Rp)</th>
                        <th class="text-center">Keputusan</th>
                    </tr>
                </thead>
                <tbody>
                    <% boolean hasData = false;
                       while(rsDraft.next()) { hasData=true; %>
                    <tr>
                        <td class="fw-bold align-middle text-primary"><%= rsDraft.getString("no_faktur") %></td>
                        <td class="align-middle"><%= rsDraft.getString("waktu") %></td>
                        
                        <td class="align-middle text-center">
                            <span class="badge bg-secondary fs-6"><%= rsDraft.getInt("total_qty") %> pcs</span>
                        </td>
                        
                        <td class="align-middle text-end text-success fw-bold">
                            <%= String.format("%,.0f", rsDraft.getDouble("total_nilai")) %>
                        </td>
                        
                        <td class="text-center">
                            <form method="POST" style="display:inline-block">
                                <input type="hidden" name="action" value="approve_faktur">
                                <input type="hidden" name="id" value="<%= rsDraft.getInt("faktur_id") %>">
                                <button class="btn btn-success btn-sm px-3">✓ Approve</button>
                            </form>

                            <form method="POST" onsubmit="return confirm('Faktur ini akan dibatalkan/hapus. Lanjut?')" style="display:inline-block">
                                <input type="hidden" name="action" value="reject_faktur">
                                <input type="hidden" name="id" value="<%= rsDraft.getInt("faktur_id") %>">
                                <button class="btn btn-outline-danger btn-sm px-3">Tolak</button>
                            </form>
                        </td>
                    </tr>
                    <% } 
                       if(!hasData) { %> 
                       <tr><td colspan="5" class="text-center py-5 text-muted">Belum ada faktur restock yang menunggu persetujuan.</td></tr> 
                    <% } %>
                </tbody>
            </table>
        </div>

    <!-- ==================== TAB 3: APPROVAL OPNAM (FISIK) ==================== -->
    <% } else if(tab.equals("opnam")) { 
       ResultSet rsOpnam = oDao.getPendingOpnames(); 
    %>
        <h5 class="mb-3 text-warning">Approval Stock Opnam</h5>
        
        
        <div class="card shadow-sm border-0">
            <table class="table table-bordered mb-0">
                <thead class="bg-secondary text-white">
                    <tr>
                        <th width="5%">ID</th>
                        <th width="15%">User</th>
                        <th>Detail Penyesuaian (Nama & Qty)</th>
                        <th width="15%">Waktu Lapor</th>
                        <th width="15%" class="text-center">Action</th>
                    </tr>
                </thead>
                <tbody>
                <% 
                   boolean exist = false;
                   if(rsOpnam != null) {
                       while(rsOpnam.next()) { 
                           exist = true; 
                %>
                <tr>
                    <td class="fw-bold align-middle text-muted">#<%= rsOpnam.getInt("opnam_id") %></td>
                    <td class="align-middle"><%= rsOpnam.getString("creator") %></td>
                    
                    <td class="align-middle fw-bold text-dark">
                        <%= rsOpnam.getString("detail_barang") != null ? rsOpnam.getString("detail_barang") : "<span class='text-danger'>-Detail Kosong-</span>" %>
                    </td>
                    
                    <td class="align-middle small text-muted"><%= rsOpnam.getString("waktu") %></td>
                    
                    <td class="text-center align-middle">
                        <form action="opnam" method="post" style="display:inline">
                            <input type="hidden" name="action" value="approve">
                            <input type="hidden" name="id" value="<%= rsOpnam.getInt("opnam_id") %>">
                            <button class="btn btn-success btn-sm" onclick="return confirm('Stok akan diupdate sesuai laporan. Lanjutkan?')">✓ Setuju</button>
                        </form>
                        <form action="opnam" method="post" style="display:inline">
                            <input type="hidden" name="action" value="reject">
                            <input type="hidden" name="id" value="<%= rsOpnam.getInt("opnam_id") %>">
                            <button class="btn btn-outline-danger btn-sm" onclick="return confirm('Tolak Laporan ini?')">✕</button>
                        </form>
                    </td>
                </tr>
                <%    }
                   }
                   if(!exist) { %>
                    <tr><td colspan="5" class="text-center py-5 text-muted">Tidak ada laporan opnam yang pending.</td></tr>
                <% } %>
                </tbody>
            </table>
        </div>
    <% } %>
</div>

<!-- ========================= MODAL EDIT PRODUK (Universal) ========================= -->
<div class="modal fade" id="editModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="product" method="post" class="modal-content shadow">
            <div class="modal-header bg-warning">
                <h5 class="modal-title">Edit Data Produk</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="update">
                
                <!-- SKU Readonly (PK) -->
                <div class="mb-3">
                    <label class="form-label text-muted small">SKU / Kode Barang</label>
                    <input type="text" name="sku" id="editSku" class="form-control bg-light" readonly>
                </div>
                
                <div class="mb-3">
                    <label class="form-label text-muted small">Nama Produk</label>
                    <input name="nama" id="editNama" class="form-control" required>
                </div>
                
                <div class="row">
                    <div class="col-6 mb-3">
                        <label class="form-label text-muted small">Harga Jual (Rp)</label>
                        <input name="harga" type="number" id="editHarga" class="form-control fw-bold" required>
                    </div>
                    <!-- FIELD EDIT STOK -->
                    <div class="col-6 mb-3">
                        <label class="form-label text-muted small">Koreksi Stok</label>
                        <input name="stok" type="number" id="editStok" class="form-control border-warning fw-bold" required>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label text-muted small">Kategori</label>
                    <select name="kategori_id" id="editKat" class="form-select">
                        <% for(Category c : cats) { %>
                            <option value="<%=c.getKategoriID()%>"><%=c.getNamaKategori()%></option>
                        <% } %>
                    </select>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                <button class="btn btn-warning fw-bold">Update Data</button>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // POPULASI DATA KE MODAL EDIT
    function editProduct(sku, nama, harga, stok, katId) {
        document.getElementById('editSku').value = sku;
        document.getElementById('editNama').value = nama;
        document.getElementById('editHarga').value = harga;
        document.getElementById('editStok').value = stok; 
        document.getElementById('editKat').value = katId;
        
        var myModal = new bootstrap.Modal(document.getElementById('editModal'));
        myModal.show();
    }
</script>
</body>
</html>