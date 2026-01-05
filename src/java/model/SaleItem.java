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
    private double subtotal; 

    public SaleItem(Product p, int qty) {
        this.produkID = p.getProdukID();
        this.qty = qty;
        this.hargaSatuan = p.getHargaJual();
        this.subtotal = qty * this.hargaSatuan;
    }

    public double getSubtotal() { 
        return qty * hargaSatuan; 
    }

    public void setProdukID(int id) { 
        this.produkID = id; 
    }
    
    public void setQty(int q) { 
        this.qty = q; 
    }
    
    public void setSubtotal(double s) { 
        this.subtotal = s; 
    }

    public int getProdukID() { 
        return produkID; 
    }
    
    public int getQty() { 
        return qty; 
    }
    
    public double getHargaSatuan() { 
        return hargaSatuan; 
    }
}
