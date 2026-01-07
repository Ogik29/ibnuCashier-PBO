<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<!DOCTYPE html>
<html>
<head>
    <title>Kasir - Point Of Sales</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; display: flex; }
        .left-panel { width: 40%; padding: 20px; background: #f4f4f4; height: 100vh; }
        .right-panel { width: 60%; padding: 20px; height: 100vh; overflow-y: auto; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; }
        input { width: 95%; padding: 10px; font-size: 16px; border: 1px solid #ccc; border-radius: 4px; }
        .btn-add { width: 100%; padding: 15px; background: #ff9800; color: white; border: none; font-size: 18px; cursor: pointer; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border-bottom: 1px solid #ddd; padding: 12px; text-align: left; }
        .total-display { font-size: 30px; font-weight: bold; text-align: right; margin-top: 20px; color: #333; }
        .error-msg { color: red; background: #ffdddd; padding: 10px; margin-bottom: 10px; }
    </style>
</head>
<body>

    <div class="left-panel">
        <h2>Kasir Input</h2>
        
        <% if(request.getAttribute("error") != null) { %>
            <div class="error-msg"><%= request.getAttribute("error") %></div>
        <% } %>
        
        <form action="proses-transaksi" method="POST">
            <input type="hidden" name="action" value="add_kasir">
            
            <div class="form-group">
                <label>Scan Barcode / SKU:</label>
                <input type="text" name="sku" id="skuField" placeholder="Contoh: 899123..." autofocus required>
            </div>
            
            <div class="form-group">
                <label>Jumlah Beli (Qty):</label>
                <input type="number" name="qty" value="1" min="1" required>
            </div>
            
            <button type="submit" class="btn-add">MASUKKAN (ENTER)</button>
        </form>
    </div>

    <div class="right-panel">
        <h2>Daftar Belanjaan</h2>
        <table>
            <thead>
                <tr>
                    <th>Barang</th>
                    <th>Harga</th>
                    <th>Qty</th>
                    <th>Subtotal</th>
                </tr>
            </thead>
            <tbody>
                <%
                    ArrayList<Object[]> cart = (ArrayList<Object[]>) session.getAttribute("cartKasir");
                    double grandTotal = 0;
                    if(cart != null) {
                        for(Object[] row : cart) {
                            String nama = (String) row[1];
                            double harga = (Double) row[2];
                            int qty = (int) row[3];
                            double subtotal = harga * qty;
                            grandTotal += subtotal;
                %>
                <tr>
                    <td><%= nama %> <br><small style="color:gray"><%= row[0] %></small></td>
                    <td><%= String.format("%,.0f", harga) %></td>
                    <td style="font-weight:bold; font-size:1.2em"><%= qty %></td>
                    <td><%= String.format("%,.0f", subtotal) %></td>
                </tr>
                <%      }
                    }
                %>
            </tbody>
        </table>

        <div class="total-display">
            Total: Rp <%= String.format("%,.0f", grandTotal) %>
        </div>
        
        <!-- Tombol Bayar / Checkout bisa Anda tambahkan di sini -->
    </div>
    
    <script>
        // Fitur kecil: Fokus kembali ke SKU setelah submit form
        window.onload = function() {
            document.getElementById("skuField").focus();
        };
    </script>
</body>
</html>