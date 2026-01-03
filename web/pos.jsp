<%-- 
    Document   : pos
    Created on : 14 Dec 2025, 15.20.26
    Author     : dimas
--%>

<%@page import="model.*, java.util.List"%>
<%
    // security check
    User u = (User) session.getAttribute("user");
    if(u == null) { 
        response.sendRedirect("index.jsp"); 
        return; 
    }
    
    List<Produk> prods = (List<Produk>) request.getAttribute("listProduk");
    List<SaleItem> carts = (List<SaleItem>) session.getAttribute("keranjang");
%>
<!DOCTYPE html>
<html>
<head><title>POS Kasir</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
<body>
    <nav class="navbar navbar-dark bg-primary p-2 mb-3">
        <span class="navbar-brand">Kasir: <%= u.getUsername() %></span>
        <form action="auth" method="post" style="display:inline">
            <input type="hidden" name="action" value="logout"><button class="btn btn-sm btn-light">Logout</button>
        </form>
    </nav>
    <div class="container-fluid">
        <div class="row">
            <div class="col-8">
                <h5>Katalog Produk</h5>
                <div class="row">
                    <% if(prods != null) for(Produk p : prods) { %>
                    <div class="col-3 mb-2">
                        <div class="card p-2">
                            <b><%= p.getNamaProduk() %></b>
                            <div>SKU: <%= p.getSKU() %></div>
                            <div>Stok: <%= p.getStok() %></div>
                            <div class="text-success fw-bold"><%= p.getHargaJual() %></div>
                            <form action="pos" method="post">
                                <input type="hidden" name="action" value="tambah">
                                <input type="hidden" name="pid" value="<%= p.getProdukID() %>">
                                <button class="btn btn-sm btn-success w-100 mt-2">Tambah</button>
                            </form>
                        </div>
                    </div>
                    <% } %>
                </div>
            </div>
            <div class="col-4">
                <div class="card">
                    <div class="card-header bg-warning">Keranjang</div>
                    <ul class="list-group list-group-flush">
                        <% double grandTotal=0; 
                           if(carts != null) for(SaleItem item : carts) { 
                           grandTotal += item.getSubtotal(); %>
                        <li class="list-group-item d-flex justify-content-between">
                            <span><%= item.getProdukRef().getNamaProduk() %> (x1)</span>
                            <span><%= item.getSubtotal() %></span>
                        </li>
                        <% } %>
                    </ul>
                    <div class="card-footer">
                        <h5>Total: <%= grandTotal %></h5>
                        <form action="pos" method="post">
                            <input type="hidden" name="action" value="checkout">
                            <button class="btn btn-primary w-100">Bayar (CASH)</button>
                        </form>
                         <form action="pos" method="post" class="mt-2">
                            <input type="hidden" name="action" value="clear">
                            <button class="btn btn-sm btn-outline-secondary w-100">Reset</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
