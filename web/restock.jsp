<%-- 
    Document   : restock
    Created on : 7 Jan 2026, 12.34.51
    Author     : dimas
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.FakturPembelian, model.FakturPembelianItem"%>
<!DOCTYPE html>
<html>
<head>
    <title>Restock Barang (Admin)</title>
    <!-- CSS (Ganti dengan link CSS local/repo jika ada) -->
    <style>
        body { font-family: sans-serif; padding: 20px; }
        .box { border: 1px solid #ccc; padding: 15px; margin-bottom: 20px; border-radius: 5px; }
        .alert { padding: 10px; background: #e0f7fa; margin-bottom: 10px; }
        .error { background: #ffebee; color: red; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background: #f2f2f2; }
        input[type="text"], input[type="number"] { padding: 5px; width: 200px; }
        button { padding: 5px 15px; cursor: pointer; background: #007bff; color: white; border: none; }
        button.green { background: #28a745; font-size: 16px; padding: 10px 20px; }
    </style>
</head>
<body>
    <h2>Input Stok Masuk (Restock Supplier)</h2>
    <a href="index.jsp">Kembali ke Menu Utama</a><br><br>

    <%-- Tampilkan Pesan Sukses/Gagal --%>
    <% if(request.getAttribute("error") != null) { %>
        <div class="alert error"><%= request.getAttribute("error") %></div>
    <% } %>
    <% if(request.getAttribute("success") != null) { %>
        <div class="alert"><%= request.getAttribute("success") %></div>
    <% } %>

    <div class="box">
        <h3>1. Input Barang</h3>
        <form action="proses-transaksi" method="POST">
            <input type="hidden" name="action" value="add_restock">
            <table>
                <tr>
                    <td>Scan SKU / Kode Barang:</td>
                    <td><input type="text" name="sku" required autofocus></td>
                </tr>
                <tr>
                    <td>Jumlah Masuk (Qty):</td>
                    <td><input type="number" name="qty" required min="1"></td>
                </tr>
                <tr>
                    <td>Harga Beli Supplier (Satuan):</td>
                    <td><input type="number" name="harga_beli" required step="100">
                    <br><small><i>Harga ini akan mengupdate harga pokok di database.</i></small></td>
                </tr>
                <tr>
                    <td></td>
                    <td><button type="submit">+ Tambah ke Daftar</button></td>
                </tr>
            </table>
        </form>
    </div>

    <div class="box">
        <h3>2. Draf Faktur Pembelian</h3>
        <table border="1">
            <thead>
                <tr>
                    <th>ID Produk</th>
                    <th>Qty</th>
                    <th>Harga Beli</th>
                    <th>Subtotal</th>
                </tr>
            </thead>
            <tbody>
                <%
                    FakturPembelian cart = (FakturPembelian) session.getAttribute("cartRestock");
                    double totalFaktur = 0;
                    if(cart != null && cart.getItems() != null) {
                        for(FakturPembelianItem item : cart.getItems()) {
                            double sub = item.getQty() * item.getHargaBeliSatuan();
                            totalFaktur += sub;
                %>
                <tr>
                    <td><%= item.getProdukID() %></td>
                    <td><%= item.getQty() %></td>
                    <td><%= String.format("%,.0f", item.getHargaBeliSatuan()) %></td>
                    <td><%= String.format("%,.0f", sub) %></td>
                </tr>
                <%      }
                    } else { out.print("<tr><td colspan='4'>Keranjang Kosong</td></tr>"); }
                %>
            </tbody>
        </table>
        
        <h3>Total Faktur: Rp <%= String.format("%,.0f", totalFaktur) %></h3>

        <form action="proses-transaksi" method="POST">
            <input type="hidden" name="action" value="save_restock">
            <label>Nomor Faktur / Surat Jalan Supplier:</label>
            <input type="text" name="noFaktur" value="INV-<%= System.currentTimeMillis() %>" required>
            <br><br>
            <button type="submit" class="green">SIMPAN STOK KE DATABASE</button>
        </form>
    </div>
</body>
</html>