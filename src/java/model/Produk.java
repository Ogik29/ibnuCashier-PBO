/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
public class Produk {
    private int produkID;
    private String sku;
    private String namaProduk;
    private double hargaBeli;
    private double hargaJual;
    private int stok;

    public Produk(int produkID, String sku, String namaProduk, double hargaBeli, double hargaJual, int stok) {
        this.produkID = produkID;
        this.sku = sku;
        this.namaProduk = namaProduk;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.stok = stok;
    }
    
    public boolean updateHarga(double hargaBaru, Admin admin) {
        if(admin != null) {
            this.hargaJual = hargaBaru;
            return true;
        }
        return false;
    }

    public int getProdukID() { 
        return produkID; 
    }
    
    public String getSKU() { 
        return sku; 
    }
    
    public String getNamaProduk() { 
        return namaProduk; 
    }
    
    public int getStok() { 
        return stok; 
    }
    
    public double getHargaJual() { 
        return hargaJual; 
    }

    public void setSku(String sku) { 
        this.sku = sku; 
    }
    
    public void setNamaProduk(String namaProduk) { 
        this.namaProduk = namaProduk; 
    }
    
    public void setHargaBeli(double hargaBeli) { 
        this.hargaBeli = hargaBeli; 
    }
    
    public void setHargaJual(double hargaJual) { 
        this.hargaJual = hargaJual; 
    }
    
    public void setStok(int stok) { 
        this.stok = stok; 
    }
}
