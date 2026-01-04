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
    private Product product; 

    public SaleItem(Product p, int qty) {
        this.product = p;
        this.produkID = p.getProdukID();
        this.hargaSatuan = p.getHargaJual();
        this.qty = qty;
    }

    public double getSubtotal() {
        return this.qty * this.hargaSatuan;
    }
    
    public int getProdukID() { 
        return produkID; 
    }
    
    public int getQty() { 
        return qty; 
    }
    
    public void setQty(int qty) { 
        this.qty = qty; 
    }
    
    public double getHargaSatuan() { 
        return hargaSatuan; 
    }
    
    public Product getProduct() { 
        return product; 
    }
}
