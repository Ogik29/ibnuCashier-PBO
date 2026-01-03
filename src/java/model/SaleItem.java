/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
public class SaleItem {
    private int produkID;
    private int qty;
    private double hargaSatuan;
    
    // Helper untuk view (Aggregation dari Produk)
    private Produk produkRef; 

    public SaleItem(Produk produk, int qty) {
        this.produkRef = produk;
        this.produkID = produk.getProdukID();
        this.qty = qty;
        this.hargaSatuan = produk.getHargaJual();
    }

    public double getSubtotal() {
        return hargaSatuan * qty;
    }
    
    public void setProdukID(int produkID) { 
        this.produkID = produkID; 
    }
    
    public void setQty(int qty) { 
        this.qty = qty; 
    }
    
    public void setSubtotal(double subtotal) { 
        // perhitungan di servlet
    }
    
    public int getQty() { 
        return qty; 
    }
    
    public Produk getProdukRef() { 
        return produkRef; 
    }
}