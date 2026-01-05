<%-- 
    Document   : sales-history
    Created on : 5 Jan 2026, 19.32.58
    Author     : dimas
--%>

<%@ page import="dao.HistoryDAO, model.User, java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    User user = (User) session.getAttribute("user");
    if(user == null) { response.sendRedirect("index.jsp"); return; }
    
    HistoryDAO historyDAO = new HistoryDAO();
    String detailStruk = request.getParameter("detail");
    List<String[]> listHistory = historyDAO.getSalesHistory();
%>
<!DOCTYPE html>
<html>
<head><title>History Penjualan</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
<body class="bg-light">
<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h3>Riwayat Transaksi Checkout</h3>
        <button onclick="history.back()" class="btn btn-secondary">Kembali</button>
    </div>

    <div class="row">
        <!-- List Faktur -->
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">Daftar Transaksi</div>
                <div class="card-body" style="height:500px; overflow-y:auto;">
                    <table class="table table-sm table-hover">
                        <thead><tr><th>No Struk</th><th>Waktu</th><th>Total</th><th>Kasir</th><th>#</th></tr></thead>
                        <tbody>
                        <% for(String[] row : listHistory) { %>
                            <tr class="<%= row[0].equals(detailStruk)?"table-active":"" %>">
                                <td><%= row[0] %></td>
                                <td><small><%= row[1] %></small></td>
                                <td><%= row[2] %></td>
                                <td><%= row[3] %></td>
                                <td><a href="?detail=<%= row[0] %>" class="btn btn-sm btn-outline-info">Detail</a></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Detail Item -->
        <div class="col-md-6">
            <% if(detailStruk != null) { 
               List<String[]> details = historyDAO.getDetailByStruk(detailStruk);
            %>
            <div class="card border-info">
                <div class="card-header bg-info text-white">Detail: <%= detailStruk %></div>
                <div class="card-body">
                    <table class="table">
                        <thead><tr><th>SKU</th><th>Nama</th><th>Qty</th><th>Subtotal</th></tr></thead>
                        <% for(String[] d : details) { %>
                        <tr>
                            <td><%= d[0] %></td><td><%= d[1] %></td><td><%= d[2] %></td><td><%= d[4] %></td>
                        </tr>
                        <% } %>
                    </table>
                </div>
            </div>
            <% } else { %>
            <div class="alert alert-secondary text-center py-5">Pilih Detail di sebelah kiri</div>
            <% } %>
        </div>
    </div>
</div>
</body>
</html>
