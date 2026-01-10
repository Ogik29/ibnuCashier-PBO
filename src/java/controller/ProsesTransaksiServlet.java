package controller;

import dao.PembelianDAO;
import model.FakturPembelian;
import model.FakturPembelianItem;
import model.Product;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "ProsesTransaksiServlet", urlPatterns = {"/proses-transaksi"})
public class ProsesTransaksiServlet extends HttpServlet {
    
    // Inisialisasi Logger
    private static final Logger LOGGER = Logger.getLogger(ProsesTransaksiServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        String pageTarget = "index.jsp"; // Default target
        HttpSession session = request.getSession();

        if ("add_restock".equals(action)) {
            String sku = request.getParameter("sku");
            
            // Validasi input angka
            int qty = 0;
            double harga = 0.0;
            try {
                qty = Integer.parseInt(request.getParameter("qty"));
                harga = Double.parseDouble(request.getParameter("harga_beli"));
            } catch (NumberFormatException e) {
                // FIX: Menangani error input angka tanpa menyebabkan crash baru saat forward
                request.setAttribute("error", "Format angka salah!");
                safeForward(request, response, "restock.jsp");
                return; // Menghentikan eksekusi method doPost di sini
            }

            PembelianDAO dao = new PembelianDAO();
            Product p = dao.cariProdukByScan(sku);

            if (p != null) {
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
                
                boolean exist = false;
                for (FakturPembelianItem i : cart.getItems()) {
                    if (i.getProdukID() == p.getProdukID()) {
                        i.setQty(i.getQty() + qty); 
                        i.setHargaBeliSatuan(harga); 
                        exist = true;
                        break;
                    }
                }
                
                if (!exist) {
                    cart.getItems().add(item);
                }
            } else {
                request.setAttribute("error", "Produk dengan SKU tersebut tidak ditemukan!");
            }
            pageTarget = "restock.jsp";

        } else if ("save_restock".equals(action)) {
            FakturPembelian cart = (FakturPembelian) session.getAttribute("cartRestock");
            String noFaktur = request.getParameter("noFaktur");
            
            if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) {
                setNoFakturHack(cart, noFaktur); 

                PembelianDAO dao = new PembelianDAO();
                if (dao.simpanRestock(cart, 1)) { // 1 = Admin Dummy
                    session.removeAttribute("cartRestock");
                    request.setAttribute("success", "Restock Berhasil! Stok telah bertambah.");
                } else {
                    request.setAttribute("error", "Gagal menyimpan database (Error DAO).");
                }
            } else {
                request.setAttribute("error", "Keranjang kosong! Tidak ada data yang disimpan.");
            }
            pageTarget = "restock.jsp";
        
        } else if ("add_kasir".equals(action)) {
            String sku = request.getParameter("sku");
            int qtyInput = 0;
            try {
                qtyInput = Integer.parseInt(request.getParameter("qty"));
            } catch(NumberFormatException e) { qtyInput = 1; }

            PembelianDAO dao = new PembelianDAO();
            Product p = dao.cariProdukByScan(sku);
            
            if (p != null) {
                if (p.getStok() < qtyInput) {
                     request.setAttribute("error", "Stok Kurang! Sisa: " + p.getStok());
                } else {
                    List<Object[]> keranjang = (List<Object[]>) session.getAttribute("cartKasir");
                    if(keranjang == null) keranjang = new ArrayList<>();
                    
                    boolean found = false;
                    for(Object[] row : keranjang) {
                         String currentSku = (String) row[0]; // [0] = SKU
                         if(currentSku.equals(p.getSKU())) { 
                             int oldQty = (int) row[3]; // [3] = Qty
                             row[3] = oldQty + qtyInput; 
                             found = true;
                             break;
                         }
                    }
                    
                    if(!found) {
                        keranjang.add(new Object[]{
                            p.getSKU(), 
                            p.getNamaProduk(), 
                            p.getHargaJual(), 
                            qtyInput, 
                            p.getProdukID()
                        });
                    }
                    session.setAttribute("cartKasir", keranjang);
                }
            } else {
                request.setAttribute("error", "Barang tidak ditemukan!");
            }
            pageTarget = "kasir.jsp";
        } else if ("clear_cart".equals(action)) {
            session.removeAttribute("cartRestock");
            pageTarget = "restock.jsp";
        }

        // Forward akhir juga dibungkus agar aman
        safeForward(request, response, pageTarget);
    }
    
    private void safeForward(HttpServletRequest req, HttpServletResponse resp, String target) {
        try {
            req.getRequestDispatcher(target).forward(req, resp);
        } catch (ServletException | IOException e) {
            // Mencatat log error forward agar aplikasi tidak crash tanpa jejak
            LOGGER.log(Level.SEVERE, "Gagal forward ke halaman: " + target, e);
        }
    }
    
    private void setNoFakturHack(FakturPembelian fp, String no) {
        try {
             java.lang.reflect.Field f = FakturPembelian.class.getDeclaredField("noFaktur");
             f.setAccessible(true);
             f.set(fp, no);
        } catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
             LOGGER.log(Level.SEVERE, "Gagal inject No Faktur via Reflection", e);
        }
    }
}