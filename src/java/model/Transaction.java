/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class Transaction {
    private int transaksiID;
    private String noStruk;
    private Timestamp waktu;
    private double total;
    private String metodeBayar;
    private List<SaleItem> items; // Composition
    private int kasirID;

    public double hitungTotal() {
        double t = 0;
        if (items != null) {
            for (SaleItem i : items) {
                t += i.getSubtotal();
            }
        }
        this.total = t;
        return t;
    }

    public void cetakStruk() {
        System.out.println("Cetak Struk: " + noStruk + " Total: " + total);
    }
    
    public void setItems(List<SaleItem> items) { 
        this.items = items; 
    }
    
    public List<SaleItem> getItems() { 
        return items; 
    }
    
    public void setTotal(double total) { 
        this.total = total; 
    }
    
    public double getTotal() { 
        return total; 
    }
    
    public void setKasirID(int id) { 
        this.kasirID = id; 
    }
    
    public int getKasirID() { 
        return kasirID; 
    }
}
