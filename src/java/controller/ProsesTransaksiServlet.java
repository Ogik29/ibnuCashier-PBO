package controller;

import dao.PembelianDAO;
import model.FakturPembelian;
import model.FakturPembelianItem;
import model.Product;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "ProsesTransaksiServlet", urlPatterns = {"/proses-transaksi"})
public class ProsesTransaksiServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        String pageTarget = "";
        HttpSession session = request.getSession();

        if ("add_restock".equals(action)) {
            // Menambah item ke Draf/Keranjang Restock (belum masuk database)
            String sku = request.getParameter("sku");
            int qty = Integer.parseInt(request.getParameter("qty"));
            double harga = Double.parseDouble(request.getParameter("harga_beli"));

            PembelianDAO dao = new PembelianDAO();
            Product p = dao.cariProdukByScan(sku);

            if (p != null) {
                // Ambil atau buat keranjang dari session
                FakturPembelian cart = (FakturPembelian) session.getAttribute("cartRestock");
                if (cart == null) {
                    cart = new FakturPembelian();
                    cart.setItems(new ArrayList<>());
                    session.setAttribute("cartRestock", cart);
                }

                FakturPembelianItem item = new FakturPembelianItem();
                item.setProdukID(p.getProdukID());
                item.setQty(qty);
                item.setHargaBeliSatuan(harga);
                
                // Opsional: Simpan nama produk di atribut item (harus extend model dulu) atau session list terpisah
                cart.getItems().add(item);
            } else {
                request.setAttribute("error", "Produk tidak ditemukan!");
            }
            pageTarget = "restock.jsp";

        } else if ("save_restock".equals(action)) {
            // Menyimpan transaksi ke Database
            FakturPembelian cart = (FakturPembelian) session.getAttribute("cartRestock");
            String noFaktur = request.getParameter("noFaktur");
            
            setNoFakturHack(cart, noFaktur);

            PembelianDAO dao = new PembelianDAO();
            if (dao.simpanRestock(cart, 1)) { // 1 = Admin ID (dummy)
                session.removeAttribute("cartRestock"); // Hapus draf
                request.setAttribute("success", "Restock Berhasil! Stok telah bertambah.");
            } else {
                request.setAttribute("error", "Gagal menyimpan database.");
            }
            pageTarget = "restock.jsp";
        
        } else if ("add_kasir".equals(action)) {
            String sku = request.getParameter("sku");
            int qtyInput = Integer.parseInt(request.getParameter("qty"));
            
            PembelianDAO dao = new PembelianDAO();
            Product p = dao.cariProdukByScan(sku);
            
            if (p != null) {
                // Logic cek stok
                if (p.getStok() < qtyInput) {
                     request.setAttribute("error", "Stok Kurang! Sisa: " + p.getStok());
                } else {
                    // Masukkan keranjang sesi
                    // Sederhanakan pakai List of Object array / Map
                    ArrayList<Object[]> keranjang = (ArrayList<Object[]>) session.getAttribute("cartKasir");
                    if(keranjang == null) keranjang = new ArrayList<>();
                    
                    // Cek jika barang sdh ada, jumlahkan qty
                    boolean found = false;
                    for(Object[] row : keranjang) {
                         if(((String)row[0]).equals(p.getSKU())) { // Index 0 = SKU
                             row[3] = (int)row[3] + qtyInput; // Index 3 = Qty
                             found = true;
                         }
                    }
                    if(!found) {
                        // Simpan: [0: SKU, 1: Nama, 2: Harga, 3: Qty, 4: ID]
                        keranjang.add(new Object[]{p.getSKU(), p.getNamaProduk(), p.getHargaJual(), qtyInput, p.getProdukID()});
                    }
                    session.setAttribute("cartKasir", keranjang);
                }
            } else {
                request.setAttribute("error", "Barang tidak ditemukan!");
            }
            pageTarget = "kasir.jsp";
        }

        // Forward kembali ke halaman yang sesuai
        request.getRequestDispatcher(pageTarget).forward(request, response);
    }
    
    // Utility karena model FakturPembelian fieldnya private
    private void setNoFakturHack(FakturPembelian fp, String no) {
        try {
             java.lang.reflect.Field f = FakturPembelian.class.getDeclaredField("noFaktur");
             f.setAccessible(true);
             f.set(fp, no);
        } catch(Exception e){}
    }
}